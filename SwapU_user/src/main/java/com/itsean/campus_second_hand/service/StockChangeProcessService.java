package com.itsean.campus_second_hand.service;

import com.itsean.campus_second_hand.dto.StockChangeMessage;

/**
 * 库存变更消息处理服务：幂等校验 + DB 异步落库
 */
public interface StockChangeProcessService {

    /**
     * 处理库存扣减消息（幂等，DB 原子扣减 + 日志状态流转）
     * @param message
     */
    void processDeduct(StockChangeMessage message);
}
