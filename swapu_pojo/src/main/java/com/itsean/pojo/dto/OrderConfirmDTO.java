package com.itsean.pojo.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * 卖家确认接单请求参数
 * <p>
 * 说明：订单状态由服务端按状态机流转，客户端传入的 status 一律忽略，这里不再接收该字段。
 */
@Data
public class OrderConfirmDTO {

    /** 订单编号，必填 */
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    /** 运费，可为 0 但不能为负 */
    @DecimalMin(value = "0", message = "运费不能为负数")
    private BigDecimal freight;
}
