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
     * 买家申请退货退款（仅待发货订单，提交后等待卖家审核）
     * @param orderRefundApplyDTO
     * @return
     */
    OrderRefundVO applyRefund(OrderRefundApplyDTO orderRefundApplyDTO);

    /**
     * 卖家审核同意退货退款（按支付方式退款）
     * @param orderRefundApproveDTO
     * @return
     */
    OrderRefundVO approveRefund(OrderRefundApproveDTO orderRefundApproveDTO);

    /**
     * 卖家拒绝退货退款（订单回到待发货）
     * @param orderRefundRejectDTO
     * @return
     */
    OrderRefundVO rejectRefund(OrderRefundRejectDTO orderRefundRejectDTO);

    /**
     * 卖家同意取消订单（待支付订单买家申请取消后）
     * @param orderCancelApproveDTO
     * @return
     */
    OrderCancelVO approveCancel(OrderCancelApproveDTO orderCancelApproveDTO);

    /**
     * 卖家拒绝取消订单（订单回到待支付）
     * @param orderCancelRejectDTO
     * @return
     */
    OrderCancelVO rejectCancel(OrderCancelRejectDTO orderCancelRejectDTO);

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

    /**
     * 超时取消订单：条件取消并回补库存，已支付订单（余额支付）同步退款
     * @param order
     * @param reason
     * @param needRefund
     * @return 是否真正取消了订单
     */
    boolean cancelTimeoutOrder(Order order, String reason, boolean needRefund);
}
