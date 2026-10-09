package com.itsean.campus_second_hand.task;

import com.itsean.campus_second_hand.entity.Product;
import com.itsean.campus_second_hand.entity.StockChangeLog;
import com.itsean.campus_second_hand.mapper.ProductMapper;
import com.itsean.campus_second_hand.mapper.StockChangeLogMapper;
import com.itsean.campus_second_hand.mq.StockChangeProducer;
import com.itsean.campus_second_hand.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存对账任务（双兜底）：
 * 1) 兜底A：以 DB 为准，扣除未落库预扣量后校准 Redis，兜底 Redis 漂移/重启；
 * 2) 兜底B：扫描超时未落库的扣减日志补投 MQ，兜底消息丢失。
 */
@Slf4j
@Component
public class StockSyncTask {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StockService stockService;
    @Autowired
    private StockChangeLogMapper stockChangeLogMapper;
    @Autowired
    private StockChangeProducer stockChangeProducer;

    /**
     * 兜底A：每 30 分钟校准 Redis 库存（Redis = DB − 未落库扣减量）
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void syncStockToRedis() {
        log.info("开始校准 Redis 库存（扣除未落库预扣量）");
        try {
            // 按商品聚合未落库的扣减量
            Map<Long, Integer> pendingDeductMap = new HashMap<>();
            for (Map<String, Object> row : stockChangeLogMapper.selectPendingDeductGroupByProduct()) {
                Long productId = ((Number) row.get("productId")).longValue();
                int pending = ((Number) row.get("pendingQuantity")).intValue();
                pendingDeductMap.put(productId, pending);
            }

            List<Product> products = productMapper.selectSellingProductStock();
            for (Product product : products) {
                int dbQuantity = product.getQuantity() == null ? 0 : product.getQuantity();
                int pending = pendingDeductMap.getOrDefault(product.getId(), 0);
                stockService.syncStock(product.getId(), dbQuantity - pending);
            }
            log.info("Redis 库存校准完成，共 {} 个在售商品，未落库扣减涉及 {} 个商品",
                    products.size(), pendingDeductMap.size());
        } catch (Exception e) {
            log.error("校准 Redis 库存失败", e);
        }
    }

    /**
     * 兜底B：每 5 分钟补投超时未落库的扣减日志
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void resendPendingStockChange() {
        log.info("开始扫描待补投的扣减日志");
        try {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
            List<StockChangeLog> pendingList = stockChangeLogMapper.selectPending(threshold);
            for (StockChangeLog pendingLog : pendingList) {
                try {
                    stockChangeProducer.sendDeduct(pendingLog.getOrderNo(), pendingLog.getProductId(), pendingLog.getQuantity());
                } catch (Exception e) {
                    log.error("补投扣减消息失败, changeNo={}", pendingLog.getChangeNo(), e);
                }
            }
            log.info("待补投扣减日志扫描完成，共 {} 条", pendingList.size());
        } catch (Exception e) {
            log.error("扫描待补投扣减日志失败", e);
        }
    }
}
