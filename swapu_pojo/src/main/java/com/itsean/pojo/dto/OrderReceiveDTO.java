package com.itsean.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 确认收货请求参数
 */
@Data
public class OrderReceiveDTO {

    /** 订单编号，必填 */
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;
}
