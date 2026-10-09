package com.itsean.campus_second_hand.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itsean.campus_second_hand.constant.StringConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置：声明库存交换机/队列/死信，配置 JSON 序列化、生产者确认与消费重试
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    /**
     * JSON 消息转换器（注册 JavaTimeModule 以序列化 LocalDateTime）
     * @return
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * 库存直连交换机
     * @return
     */
    @Bean
    public DirectExchange stockExchange() {
        return new DirectExchange(StringConstant.EXCHANGE_STOCK, true, false);
    }

    /**
     * 库存死信交换机
     * @return
     */
    @Bean
    public DirectExchange stockDlxExchange() {
        return new DirectExchange(StringConstant.EXCHANGE_STOCK_DLX, true, false);
    }

    /**
     * 库存同步主队列（绑定死信交换机，重试耗尽后转入死信队列）
     * @return
     */
    @Bean
    public Queue stockSyncQueue() {
        Map<String, Object> args = new HashMap<>(4);
        args.put("x-dead-letter-exchange", StringConstant.EXCHANGE_STOCK_DLX);
        args.put("x-dead-letter-routing-key", StringConstant.ROUTING_KEY_STOCK_DEAD);
        return new Queue(StringConstant.QUEUE_STOCK_SYNC, true, false, false, args);
    }

    /**
     * 库存死信队列
     * @return
     */
    @Bean
    public Queue stockDlqQueue() {
        return new Queue(StringConstant.QUEUE_STOCK_DLQ, true, false, false);
    }

    /**
     * 主队列绑定到主交换机
     * @param stockSyncQueue
     * @param stockExchange
     * @return
     */
    @Bean
    public Binding stockSyncBinding(Queue stockSyncQueue, DirectExchange stockExchange) {
        return BindingBuilder.bind(stockSyncQueue).to(stockExchange).with(StringConstant.ROUTING_KEY_STOCK_CHANGE);
    }

    /**
     * 死信队列绑定到死信交换机
     * @param stockDlqQueue
     * @param stockDlxExchange
     * @return
     */
    @Bean
    public Binding stockDlqBinding(Queue stockDlqQueue, DirectExchange stockDlxExchange) {
        return BindingBuilder.bind(stockDlqQueue).to(stockDlxExchange).with(StringConstant.ROUTING_KEY_STOCK_DEAD);
    }

    /**
     * RabbitTemplate：JSON 序列化 + 生产者确认/回退日志
     * @param connectionFactory
     * @param jackson2JsonMessageConverter
     * @return
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter);
        template.setMandatory(true);
        // 生产者确认：仅记录未确认，真正的补投交由对账任务兜底
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.warn("库存消息发送未确认, changeNo={}, cause={}",
                        correlationData == null ? null : correlationData.getId(), cause);
            }
        });
        // 路由失败回退日志
        template.setReturnsCallback(returned -> {
            log.warn("库存消息路由失败, exchange={}, routingKey={}, replyCode={}, replyText={}",
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyCode(), returned.getReplyText());
        });
        return template;
    }

    /**
     * 重试拦截器：失败重试 5 次（指数退避 1s~10s），最终转入死信队列
     * @param rabbitTemplate
     * @return
     */
    @Bean
    public RetryOperationsInterceptor stockRetryInterceptor(RabbitTemplate rabbitTemplate) {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(5)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RepublishMessageRecoverer(rabbitTemplate,
                        StringConstant.EXCHANGE_STOCK_DLX,
                        StringConstant.ROUTING_KEY_STOCK_DEAD))
                .build();
    }

    /**
     * 库存监听容器工厂：自动 ack + 重试拦截 + 拒绝不重回队列（避免死循环）
     * @param configurer
     * @param connectionFactory
     * @param stockRetryInterceptor
     * @return
     */
    @Bean("stockRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory stockRabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            RetryOperationsInterceptor stockRetryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(stockRetryInterceptor);
        return factory;
    }

}
