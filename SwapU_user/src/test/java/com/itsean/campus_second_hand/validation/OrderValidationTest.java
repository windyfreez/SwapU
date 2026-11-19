package com.itsean.campus_second_hand.validation;

import com.itsean.pojo.dto.OrderConfirmDTO;
import com.itsean.pojo.dto.OrderDTO;
import com.itsean.pojo.dto.OrderPayDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 订单 DTO 参数校验测试
 * <p>
 * 不启动 Spring 容器：直接用 Bean Validation 校验 DTO，
 * 确认 @NotNull / @Min 等约束确实生效（避免"加了注解但没生效"）。
 */
class OrderValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private OrderDTO validOrder() {
        OrderDTO dto = new OrderDTO();
        dto.setProductId(1L);
        dto.setQuantity(1);
        return dto;
    }

    @Test
    @DisplayName("正常下单参数通过校验")
    void validOrderPasses() {
        Set<ConstraintViolation<OrderDTO>> violations = validator.validate(validOrder());
        assertTrue(violations.isEmpty(), "合法参数不应产生校验错误");
    }

    @Test
    @DisplayName("购买数量为 0 被拒绝")
    void zeroQuantityRejected() {
        OrderDTO dto = validOrder();
        dto.setQuantity(0);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("购买数量为负数被拒绝（否则会反向加库存且金额为负）")
    void negativeQuantityRejected() {
        OrderDTO dto = validOrder();
        dto.setQuantity(-5);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("商品ID为空被拒绝")
    void nullProductIdRejected() {
        OrderDTO dto = validOrder();
        dto.setProductId(null);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("配送方式不传仍可通过（由 service 兜底为快递）")
    void nullDeliveryMethodAllowed() {
        OrderDTO dto = validOrder();
        dto.setDeliveryMethod(null);
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("配送方式越界被拒绝")
    void illegalDeliveryMethodRejected() {
        OrderDTO dto = validOrder();
        dto.setDeliveryMethod(9);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("订单号为空时支付参数被拒绝")
    void blankOrderNoRejected() {
        OrderPayDTO dto = new OrderPayDTO();
        dto.setPayType(3);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("负数运费被拒绝")
    void negativeFreightRejected() {
        OrderConfirmDTO dto = new OrderConfirmDTO();
        dto.setOrderNo("2026010100000001");
        dto.setFreight(new BigDecimal("-1"));
        assertFalse(validator.validate(dto).isEmpty());
    }
}
