package com.itsean.campus_second_hand.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 卖家拒绝取消订单请求参数
 */
@Data
public class OrderCancelRejectDTO {

    /** 订单编号，必填 */
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    /** 拒绝原因（随系统消息发给买家） */
    @Size(max = 500, message = "拒绝原因不能超过500个字符")
    private String rejectReason;

}
