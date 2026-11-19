package com.itsean.campus_second_hand.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 申请退货退款请求参数
 */
@Data
public class OrderRefundApplyDTO {

    /** 订单编号，必填 */
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    /** 退货原因（随系统消息发给卖家），对应 cancel_reason varchar(500) */
    @Size(max = 500, message = "退货原因不能超过500个字符")
    private String refundReason;

}
