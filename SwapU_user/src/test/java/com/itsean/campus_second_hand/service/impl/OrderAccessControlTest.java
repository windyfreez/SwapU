package com.itsean.campus_second_hand.service.impl;

import com.itsean.campus_second_hand.mapper.OrderMapper;
import com.itsean.campus_second_hand.mapper.UserMapper;
import com.itsean.common.constant.MessageConstant;
import com.itsean.common.context.BaseContext;
import com.itsean.common.exception.OrderException;
import com.itsean.pojo.dto.OrderDeliverDTO;
import com.itsean.pojo.dto.OrderPayDTO;
import com.itsean.pojo.dto.OrderReceiveDTO;
import com.itsean.pojo.entity.Order;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * 订单接口水平越权（IDOR）防护测试
 * <p>
 * 不启动 Spring 容器、不连数据库：用 Mockito 替换 Mapper，
 * 只验证「当前登录用户与订单归属关系」是否被正确校验。
 */
@ExtendWith(MockitoExtension.class)
class OrderAccessControlTest {

    private static final Long BUYER_ID = 1001L;
    private static final Long SELLER_ID = 2002L;
    private static final Long STRANGER_ID = 3003L;
    private static final String ORDER_NO = "2026010100000001";

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @AfterEach
    void clearContext() {
        BaseContext.removeCurrentId();
    }

    private Order buildOrder() {
        Order order = new Order();
        order.setOrderNo(ORDER_NO);
        order.setBuyerId(BUYER_ID);
        order.setSellerId(SELLER_ID);
        order.setQuantity(1);
        order.setStatus(Order.ORDER_STATUS_WAIT_PAY);
        return order;
    }

    @Test
    @DisplayName("买家可以查询自己的订单")
    void buyerCanReadOwnOrder() {
        when(orderMapper.getOrderByOrderNo(ORDER_NO)).thenReturn(buildOrder());
        BaseContext.setCurrentId(BUYER_ID);

        assertNotNull(orderService.getOrderByOrderNo(ORDER_NO));
    }

    @Test
    @DisplayName("卖家可以查询自己的订单")
    void sellerCanReadOwnOrder() {
        when(orderMapper.getOrderByOrderNo(ORDER_NO)).thenReturn(buildOrder());
        BaseContext.setCurrentId(SELLER_ID);

        assertNotNull(orderService.getOrderByOrderNo(ORDER_NO));
    }

    @Test
    @DisplayName("第三方拿单号查询他人订单被拒绝")
    void strangerCannotReadOthersOrder() {
        when(orderMapper.getOrderByOrderNo(ORDER_NO)).thenReturn(buildOrder());
        BaseContext.setCurrentId(STRANGER_ID);

        OrderException ex = assertThrows(OrderException.class,
                () -> orderService.getOrderByOrderNo(ORDER_NO));
        assertEquals(MessageConstant.CANT_VIEW_OTHERS_ORDER, ex.getMessage());
    }

    @Test
    @DisplayName("第三方不能查看他人订单详情")
    void strangerCannotReadOrderDetail() {
        when(orderMapper.getOrderByOrderNo(ORDER_NO)).thenReturn(buildOrder());
        BaseContext.setCurrentId(STRANGER_ID);

        OrderException ex = assertThrows(OrderException.class,
                () -> orderService.getOrderDetail(ORDER_NO));
        assertEquals(MessageConstant.CANT_VIEW_OTHERS_ORDER, ex.getMessage());
    }

    @Test
    @DisplayName("非卖家不能发货")
    void strangerCannotDeliver() {
        when(orderMapper.getOrderByOrderNo(ORDER_NO)).thenReturn(buildOrder());
        BaseContext.setCurrentId(STRANGER_ID);

        OrderDeliverDTO dto = new OrderDeliverDTO();
        dto.setOrderNo(ORDER_NO);

        OrderException ex = assertThrows(OrderException.class, () -> orderService.deliverOrder(dto));
        assertEquals(MessageConstant.ONLY_SELLER_CAN_DELIVER, ex.getMessage());
    }

    @Test
    @DisplayName("买家不能越权替卖家发货")
    void buyerCannotDeliver() {
        when(orderMapper.getOrderByOrderNo(ORDER_NO)).thenReturn(buildOrder());
        BaseContext.setCurrentId(BUYER_ID);

        OrderDeliverDTO dto = new OrderDeliverDTO();
        dto.setOrderNo(ORDER_NO);

        OrderException ex = assertThrows(OrderException.class, () -> orderService.deliverOrder(dto));
        assertEquals(MessageConstant.ONLY_SELLER_CAN_DELIVER, ex.getMessage());
    }

    @Test
    @DisplayName("非买家不能支付他人订单")
    void strangerCannotPay() {
        when(orderMapper.getOrderByOrderNo(ORDER_NO)).thenReturn(buildOrder());
        BaseContext.setCurrentId(STRANGER_ID);

        OrderPayDTO dto = new OrderPayDTO();
        dto.setOrderNo(ORDER_NO);
        dto.setPayType(Order.PAY_TYPE_BALANCE);

        OrderException ex = assertThrows(OrderException.class, () -> orderService.payOrder(dto));
        assertEquals(MessageConstant.ONLY_BUYER_CAN_PAY, ex.getMessage());
    }

    @Test
    @DisplayName("非买家不能确认收货")
    void strangerCannotReceive() {
        when(orderMapper.getOrderByOrderNo(ORDER_NO)).thenReturn(buildOrder());
        BaseContext.setCurrentId(SELLER_ID);

        OrderReceiveDTO dto = new OrderReceiveDTO();
        dto.setOrderNo(ORDER_NO);

        OrderException ex = assertThrows(OrderException.class, () -> orderService.receiveOrder(dto));
        assertEquals(MessageConstant.ONLY_BUYER_CAN_RECEIVE, ex.getMessage());
    }

    @Test
    @DisplayName("订单不存在时返回业务异常而不是空指针")
    void missingOrderThrowsBusinessException() {
        when(orderMapper.getOrderByOrderNo(ORDER_NO)).thenReturn(null);
        BaseContext.setCurrentId(BUYER_ID);

        OrderException ex = assertThrows(OrderException.class,
                () -> orderService.getOrderByOrderNo(ORDER_NO));
        assertEquals(MessageConstant.ORDER_NOT_EXIST, ex.getMessage());
    }
}
