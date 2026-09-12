package com.itsean.campus_second_hand.dto;

import lombok.Data;

/**
 * 卖家拒绝退货退款请求参数
 */
@Data
public class OrderRefundRejectDTO {

    private String orderNo;//订单编号

    private String rejectReason;//拒绝原因（随系统消息发给买家）

}
