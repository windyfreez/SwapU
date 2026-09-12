package com.itsean.campus_second_hand.dto;

import lombok.Data;

/**
 * 申请退货退款请求参数
 */
@Data
public class OrderRefundApplyDTO {

    private String orderNo;//订单编号

    private String refundReason;//退货原因（随系统消息发给卖家）

}
