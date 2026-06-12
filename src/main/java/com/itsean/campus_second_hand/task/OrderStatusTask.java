package com.itsean.campus_second_hand.task;

import com.itsean.campus_second_hand.entity.Order;
import com.itsean.campus_second_hand.entity.User;
import com.itsean.campus_second_hand.mapper.OrderMapper;
import com.itsean.campus_second_hand.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
public class OrderStatusTask {

    public static final String ORDER_PAY_TIMEOUT = "超时未支付订单";
    public static final String ORDER_CONFIRM_TIMEOUT = "超时未确认订单";
    private static final String ORDER_DELIVER_TIMEOUT = "超时订单未发货";

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 超时未确认订单取消
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional// 开启事务，需要写入订单和用户两个表的属性
    public void cancelOvertimeConfirmOrder(){
        log.info("开始取消超时未确认订单,{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        List<Order> orderList = orderMapper.cancelOvertimeOrder(Order.ORDER_STATUS_WAIT_ACCEPT,time);

        if(orderList != null && !orderList.isEmpty()){
            for (Order order : orderList) {
                order.setStatus(Order.ORDER_STATUS_CANCEL);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason(ORDER_CONFIRM_TIMEOUT);
                orderMapper.update(order);

                //超时未确认订单信誉分扣除
                setCreditScore(order.getSellerId(), ORDER_CONFIRM_TIMEOUT);
            }
        }
    }

    /**
     * 超时未支付订单取消
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional// 开启事务，需要写入订单和用户两个表的属性
    public void cancelOvertimePayOrder(){
        log.info("开始取消超时未支付订单,{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        List<Order> orderList = orderMapper.cancelOvertimeOrder(Order.ORDER_STATUS_WAIT_PAY,time);

        if(orderList != null && !orderList.isEmpty()){
            for (Order order : orderList) {
                order.setStatus(Order.ORDER_STATUS_CANCEL);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason(ORDER_PAY_TIMEOUT);
                orderMapper.update(order);

                //超时未支付订单信誉分扣除
                setCreditScore(order.getBuyerId(), ORDER_PAY_TIMEOUT);
            }
        }
    }

    /**
     * 一周内未发货订单取消
     */
    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public void cancelOvertimeDeliverOrder(){
        log.info("开始取消一周内未发货订单,{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusDays(-7);
        List<Order> orderList = orderMapper.cancelOvertimeOrder(Order.ORDER_STATUS_WAIT_DELIVER,time);
        if(orderList != null && !orderList.isEmpty()){
            for (Order order : orderList) {
                order.setStatus(Order.ORDER_STATUS_CANCEL);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason(ORDER_DELIVER_TIMEOUT);
                orderMapper.update(order);

                //一周内未发货订单信誉分扣除
                setCreditScore(order.getSellerId(), ORDER_DELIVER_TIMEOUT);
            }
        }
    }

    /**
     * 扣除用户信誉分
     * @param userId
     * @param reason
     */
    public void setCreditScore(Long userId , String reason){
        User user = userMapper.findById(userId);

        Integer creditScore = user.getCreditScore();
        user.setCreditScore(creditScore - 2);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);

        log.info("用户{}信誉分减2，信誉分剩余{}，信誉分扣除原因{}",userId,user.getCreditScore(),reason);


    }

}
