package com.itsean.campus_second_hand.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 卖家同意取消订单请求参数
 */
@Data
public class OrderCancelApproveDTO {

    /** 订单编号，必填 */
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

}
