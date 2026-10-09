package com.itsean.campus_second_hand.mq;

import com.itsean.campus_second_hand.constant.NumberConstant;
import com.itsean.campus_second_hand.dto.StockChangeMessage;
import com.itsean.campus_second_hand.service.StockChangeProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 库存变更消息消费者：监听扣减队列，交由处理服务异步落库
 */
@Slf4j
@Component
public class StockChangeConsumer {

    @Autowired
    private StockChangeProcessService stockChangeProcessService;

    /**
     * 监听库存同步队列，处理扣减消息（回补走同步，不经过此队列）
     * @param message
     */
    @RabbitListener(queues = "#{T(com.itsean.campus_second_hand.constant.StringConstant).QUEUE_STOCK_SYNC}",
            containerFactory = "stockRabbitListenerContainerFactory")
    public void onStockChange(StockChangeMessage message) {
        log.info("收到库存变更消息, changeNo={}, operationType={}", message.getChangeNo(), message.getOperationType());
        if (message.getOperationType() == null || NumberConstant.STOCK_CHANGE_OP_DEDUCT != message.getOperationType()) {
            log.warn("收到非扣减消息，当前仅处理扣减，忽略, changeNo={}", message.getChangeNo());
            return;
        }
        stockChangeProcessService.processDeduct(message);
    }
}
