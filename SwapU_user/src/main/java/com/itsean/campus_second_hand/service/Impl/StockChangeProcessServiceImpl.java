package com.itsean.campus_second_hand.service.impl;

import com.itsean.campus_second_hand.constant.NumberConstant;
import com.itsean.campus_second_hand.dto.StockChangeMessage;
import com.itsean.campus_second_hand.entity.StockChangeLog;
import com.itsean.campus_second_hand.mapper.ProductMapper;
import com.itsean.campus_second_hand.mapper.StockChangeLogMapper;
import com.itsean.campus_second_hand.service.StockChangeProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 库存变更消息处理实现：DB 原子扣减与日志状态流转在同一事务，保证恰好一次落库
 */
@Slf4j
@Service
public class StockChangeProcessServiceImpl implements StockChangeProcessService {

    @Autowired
    private StockChangeLogMapper stockChangeLogMapper;
    @Autowired
    private ProductMapper productMapper;

    /**
     * 处理扣减消息：幂等校验后 DB 原子扣减，成功/失败均更新日志状态
     * @param message
     */
    @Override
    @Transactional
    public void processDeduct(StockChangeMessage message) {
        StockChangeLog exist = stockChangeLogMapper.getByChangeNo(message.getChangeNo());
        if (exist == null) {
            // 订单侧已先行落日志，此处不存在属异常，防御性跳过
            log.warn("库存变更日志不存在，跳过处理, changeNo={}", message.getChangeNo());
            return;
        }
        if (NumberConstant.STOCK_CHANGE_STATUS_SUCCESS == exist.getStatus()) {
            // 已成功，幂等跳过
            log.info("库存变更日志已处理成功，幂等跳过, changeNo={}", message.getChangeNo());
            return;
        }
        if (NumberConstant.STOCK_CHANGE_STATUS_FAILED == exist.getStatus()) {
            // 已标记失败（如库存不足），不再自动重试，交由人工/补偿
            log.warn("库存变更日志已失败，跳过自动处理, changeNo={}", message.getChangeNo());
            return;
        }

        // status=0 待处理：执行 DB 原子扣减（复用带防超卖守卫的 SQL）
        int affected = productMapper.deductStock(message.getProductId(), message.getQuantity(), LocalDateTime.now());
        if (affected != 1) {
            // DB 实际库存不足，说明 Redis 漂移（预扣超前），标记失败并告警，等待补偿
            stockChangeLogMapper.updateStatus(message.getChangeNo(),
                    NumberConstant.STOCK_CHANGE_STATUS_FAILED, LocalDateTime.now());
            log.error("异步扣减库存失败：DB 实际库存不足, changeNo={}, orderNo={}, productId={}, quantity={}",
                    message.getChangeNo(), message.getOrderNo(), message.getProductId(), message.getQuantity());
            return;
        }

        stockChangeLogMapper.updateStatus(message.getChangeNo(),
                NumberConstant.STOCK_CHANGE_STATUS_SUCCESS, LocalDateTime.now());
        log.info("异步扣减库存成功, changeNo={}, orderNo={}, productId={}, quantity={}",
                message.getChangeNo(), message.getOrderNo(), message.getProductId(), message.getQuantity());
    }
}
