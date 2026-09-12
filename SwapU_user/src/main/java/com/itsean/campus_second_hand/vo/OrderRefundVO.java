package com.itsean.campus_second_hand.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退货退款结果VO
 */
@Data
public class OrderRefundVO {

    private String orderNo;//订单编号

    private Integer status;//订单状态：7退货审核中 8已退货退款

    private String statusDesc;//订单状态描述

    private Integer payType;//支付方式：1支付宝 2微信 3余额

    private BigDecimal refundAmount;//退款金额

    private String refundReason;//退货原因

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime refundTime;//退款时间

}
