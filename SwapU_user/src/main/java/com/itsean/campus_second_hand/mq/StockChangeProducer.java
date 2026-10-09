package com.itsean.campus_second_hand.mq;

import com.itsean.campus_second_hand.constant.NumberConstant;
import com.itsean.campus_second_hand.constant.StringConstant;
import com.itsean.campus_second_hand.dto.StockChangeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 库存变更消息生产者：将扣减消息投递到 RabbitMQ，由消费者异步落库
 */
@Slf4j
@Component
public class StockChangeProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送扣减库存消息
     * @param orderNo
     * @param productId
     * @param quantity
     */
    public void sendDeduct(String orderNo, Long productId, Integer quantity) {
        StockChangeMessage message = new StockChangeMessage();
        message.setChangeNo(orderNo + StringConstant.STOCK_CHANGE_NO_DEDUCT_SUFFIX);
        message.setOrderNo(orderNo);
        message.setProductId(productId);
        message.setQuantity(quantity);
        message.setOperationType(NumberConstant.STOCK_CHANGE_OP_DEDUCT);
        message.setCreateTime(LocalDateTime.now());

        rabbitTemplate.convertAndSend(
                StringConstant.EXCHANGE_STOCK,
                StringConstant.ROUTING_KEY_STOCK_CHANGE,
                message,
                new CorrelationData(message.getChangeNo()));
        log.info("发送库存扣减消息, changeNo={}, orderNo={}", message.getChangeNo(), orderNo);
    }

}
