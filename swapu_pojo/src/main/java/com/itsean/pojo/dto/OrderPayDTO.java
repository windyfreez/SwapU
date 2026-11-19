package com.itsean.pojo.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 订单支付请求参数
 */
@Data
public class OrderPayDTO {

    /** 订单编号，必填 */
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    /** 支付方式：1支付宝 2微信 3余额 */
    @NotNull(message = "支付方式不能为空")
    @Min(value = 1, message = "支付方式取值不合法")
    @Max(value = 3, message = "支付方式取值不合法")
    private Integer payType;

    /** 支付密码，余额支付时必填（由 service 层按支付方式校验） */
    @Size(max = 32, message = "支付密码长度不合法")
    private String payPassword;
}
