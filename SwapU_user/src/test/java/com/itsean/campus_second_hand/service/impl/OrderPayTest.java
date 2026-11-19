package com.itsean.campus_second_hand.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.itsean.campus_second_hand.mapper.OrderMapper;
import com.itsean.campus_second_hand.mapper.UserMapper;
import com.itsean.common.constant.MessageConstant;
import com.itsean.common.context.BaseContext;
import com.itsean.common.exception.OrderException;
import com.itsean.pojo.dto.OrderPayDTO;
import com.itsean.pojo.entity.Order;
import com.itsean.pojo.entity.User;
import com.itsean.pojo.vo.OrderPayVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单支付测试：支付密码校验（BCrypt / 历史账号惰性升级）与并发重复支付防护
 * <p>
 * 不启动 Spring 容器、不连数据库。
 */
@ExtendWith(MockitoExtension.class)
class OrderPayTest {

    private static final Long BUYER_ID = 1001L;
    private static final Long SELLER_ID = 2002L;
    private static final String ORDER_NO = "2026010100000002";
    private static final String LOGIN_PASSWORD = "login123";
    private static final String PAY_PASSWORD = "pay8866";

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private User buyer;
    private User seller;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setOrderNo(ORDER_NO);
        order.setBuyerId(BUYER_ID);
        order.setSellerId(SELLER_ID);
        order.setStatus(Order.ORDER_STATUS_WAIT_PAY);
        order.setQuantity(1);
        order.setAmount(new BigDecimal("100.00"));
        order.setTotalAmount(new BigDecimal("100.00"));

        buyer = new User();
        buyer.setId(BUYER_ID);
        buyer.setUsername("buyer");
        buyer.setPassword(DigestUtils.md5DigestAsHex(LOGIN_PASSWORD.getBytes()));
        buyer.setPayPassword(BCrypt.hashpw(PAY_PASSWORD));
        buyer.setBalance(new BigDecimal("200.00"));

        seller = new User();
        seller.setId(SELLER_ID);
        seller.setUsername("seller");
        seller.setBalance(new BigDecimal("0.00"));

        BaseContext.setCurrentId(BUYER_ID);
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    private void stubOrderAndUsers() {
        when(orderMapper.getOrderByOrderNo(ORDER_NO)).thenReturn(order);
        when(userMapper.findById(BUYER_ID)).thenReturn(buyer);
        when(userMapper.findById(SELLER_ID)).thenReturn(seller);
    }

    private void stubClaim(int affected) {
        when(orderMapper.payIfMatch(eq(ORDER_NO),
                eq(Order.ORDER_STATUS_WAIT_PAY),
                eq(Order.PAY_TYPE_BALANCE),
                any(LocalDateTime.class),
                eq(Order.ORDER_STATUS_WAIT_DELIVER))).thenReturn(affected);
    }

    private OrderPayDTO payDTO(String payPassword) {
        OrderPayDTO dto = new OrderPayDTO();
        dto.setOrderNo(ORDER_NO);
        dto.setPayType(Order.PAY_TYPE_BALANCE);
        dto.setPayPassword(payPassword);
        return dto;
    }

    @Test
    @DisplayName("支付密码正确时完成支付并完成余额划转")
    void correctPayPasswordCompletesPayment() {
        stubOrderAndUsers();
        stubClaim(1);

        OrderPayVO vo = orderService.payOrder(payDTO(PAY_PASSWORD));

        assertNotNull(vo);
        assertEquals(Order.ORDER_STATUS_WAIT_DELIVER, vo.getStatus());
        assertEquals(0, buyer.getBalance().compareTo(new BigDecimal("100.00")));
        assertEquals(0, seller.getBalance().compareTo(new BigDecimal("100.00")));
        verify(userMapper).update(buyer);
        verify(userMapper).update(seller);
    }

    @Test
    @DisplayName("支付密码错误时拒绝支付，且不推进订单状态")
    void wrongPayPasswordRejected() {
        stubOrderAndUsers();

        OrderException ex = assertThrows(OrderException.class, () -> orderService.payOrder(payDTO("wrong123")));

        assertEquals(MessageConstant.PASSWORD_ERROR, ex.getMessage());
        verify(orderMapper, never()).payIfMatch(any(), any(), any(), any(), any());
        assertTrue(buyer.getBalance().compareTo(new BigDecimal("200.00")) == 0);
    }

    @Test
    @DisplayName("设置过支付密码后，登录密码不能再用于支付（两者已独立）")
    void loginPasswordNoLongerAccepted() {
        stubOrderAndUsers();

        OrderException ex = assertThrows(OrderException.class, () -> orderService.payOrder(payDTO(LOGIN_PASSWORD)));

        assertEquals(MessageConstant.PASSWORD_ERROR, ex.getMessage());
        verify(orderMapper, never()).payIfMatch(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("历史账号未初始化支付密码时，用登录密码校验通过并升级为 BCrypt")
    void legacyAccountUpgradesToBCrypt() {
        buyer.setPayPassword(null);
        stubOrderAndUsers();
        stubClaim(1);

        orderService.payOrder(payDTO(LOGIN_PASSWORD));

        assertNotNull(buyer.getPayPassword(), "支付密码应被升级为 BCrypt 密文");
        assertTrue(buyer.getPayPassword().startsWith("$2"), "升级后的密文应以 $2 开头");
        assertTrue(BCrypt.checkpw(LOGIN_PASSWORD, buyer.getPayPassword()));
    }

    @Test
    @DisplayName("余额不足时拒绝支付，且不推进订单状态")
    void insufficientBalanceRejected() {
        buyer.setBalance(new BigDecimal("50.00"));
        stubOrderAndUsers();

        OrderException ex = assertThrows(OrderException.class, () -> orderService.payOrder(payDTO(PAY_PASSWORD)));

        assertEquals(MessageConstant.BALANCE_NOT_ENOUGH, ex.getMessage());
        // 余额校验发生在状态抢占之前，payIfMatch 不应被调用
        verify(orderMapper, never()).payIfMatch(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("并发重复支付：状态抢占失败时不扣款")
    void duplicatePaymentRejectedWithoutDeduction() {
        stubOrderAndUsers();
        stubClaim(0);

        OrderException ex = assertThrows(OrderException.class, () -> orderService.payOrder(payDTO(PAY_PASSWORD)));

        assertEquals(MessageConstant.ORDER_STATUS_CANT_PAY, ex.getMessage());
        verify(userMapper, never()).update(any(User.class));
        assertTrue(buyer.getBalance().compareTo(new BigDecimal("200.00")) == 0, "抢占失败不应扣款");
    }
}
