package com.itsean.campus_second_hand.service;

import com.itsean.campus_second_hand.dto.*;
import com.itsean.campus_second_hand.entity.Order;
import com.itsean.campus_second_hand.entity.result.PageResult;
import com.itsean.campus_second_hand.vo.*;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    OrderVO createOrder(OrderDTO orderDTO);

    /**
     * 确认订单
     * @param orderConfirmDTO
     * @return
     */
    OrderConfirmVO confirmOrder(OrderConfirmDTO orderConfirmDTO);

    /**
     * 根据订单编号查询订单信息
     * @param orderNo
     * @return
     */
    Order getOrderByOrderNo(String orderNo);

    /**
     * 取消订单
     * @param orderCancelDTO
     * @return
     */
    OrderCancelVO cancelOrder(OrderCancelDTO orderCancelDTO);

    /**
     * 支付订单
     * @param orderPayDTO
     * @return
     */
    OrderPayVO payOrder(OrderPayDTO orderPayDTO);

    /**
     * 订单发货
     * @param orderDeliverDTO
     * @return
     */
    OrderDeliverVO deliverOrder(OrderDeliverDTO orderDeliverDTO);

    /**
     * 确认收货
     * @param orderReceiveDTO
     * @return
     */
    OrderReceiveVO receiveOrder(OrderReceiveDTO orderReceiveDTO);

    /**
     * 获取订单详情
     * @param orderNo
     * @return
     */
    OrderDetailVO getOrderDetail(String orderNo);

    /**
     * 分页获取当前用户所有订单
     * @param orderPageQueryDTO
     * @return
     */
    PageResult pageQuery(OrderPageQueryDTO orderPageQueryDTO);

    /**
     * 获取订单统计信息
     * @return
     */
    OrderStatisticsVO getOrderStatistics();
}
