package com.itsean.campus_second_hand.task;

import com.itsean.campus_second_hand.entity.Product;
import com.itsean.campus_second_hand.mapper.ProductMapper;
import com.itsean.campus_second_hand.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 库存对账任务：以 DB 为准，周期将 Redis 库存与 DB 对齐，兜底 Redis 宕机/漂移造成的偏差
 */
@Slf4j
@Component
public class StockSyncTask {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StockService stockService;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void syncStockToRedis() {
        log.info("开始同步库存到 Redis");
        try {
            List<Product> products = productMapper.selectSellingProductStock();
            for (Product product : products) {
                int quantity = product.getQuantity() == null ? 0 : product.getQuantity();
                stockService.syncStock(product.getId(), quantity);
            }
            log.info("库存同步完成，共 {} 个在售商品", products.size());
        } catch (Exception e) {
            log.error("同步库存到 Redis 失败", e);
        }
    }
}
