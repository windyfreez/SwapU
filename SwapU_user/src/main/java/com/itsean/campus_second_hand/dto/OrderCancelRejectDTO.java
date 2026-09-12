package com.itsean.campus_second_hand.dto;

import lombok.Data;

/**
 * 卖家拒绝取消订单请求参数
 */
@Data
public class OrderCancelRejectDTO {

    private String orderNo;//订单编号

    private String rejectReason;//拒绝原因（随系统消息发给买家）

}
