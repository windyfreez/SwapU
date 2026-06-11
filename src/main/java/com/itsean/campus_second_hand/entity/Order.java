package com.itsean.campus_second_hand.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    //1.订单状态数字常量
    public static final Integer ORDER_STATUS_WAIT_ACCEPT = 1;//待确认订单
    public static final Integer ORDER_STATUS_WAIT_PAY = 2;//待支付
    public static final Integer ORDER_STATUS_WAIT_DELIVER = 3;//待发货
    public static final Integer ORDER_STATUS_WAIT_RECEIVE = 4;//待收货
    public static final Integer ORDER_STATUS_ALREADY_RECEIVE = 5;//已收货
    public static final Integer ORDER_STATUS_CANCEL = 6;//取消订单
    //2.订单状态描述
    public static final String ORDER_STATUS_WAIT_ACCEPT_DESC = "待确认订单";
    public static final String ORDER_STATUS_WAIT_PAY_DESC = "待支付";
    public static final String ORDER_STATUS_WAIT_DELIVER_DESC = "待发货";
    public static final String ORDER_STATUS_WAIT_RECEIVE_DESC = "待收货";
    public static final String ORDER_STATUS_ALREADY_RECEIVE_DESC = "已收货";
    public static final String ORDER_STATUS_CANCEL_DESC = "取消订单";
    //3.支付方式
    public static final Integer PAY_TYPE_ALIPAY = 1;//支付宝
    public static final Integer PAY_TYPE_WECHAT = 2;//微信
    public static final Integer PAY_TYPE_BALANCE = 3;//余额支付
    //4.交货方式
    public static final Integer DELIVERY_METHOD_EXPRESS = 1;//快递
    public static final Integer DELIVERY_METHOD_TAKEOUT = 2;//外卖跑腿
    public static final Integer DELIVERY_METHOD_SELF_PICKUP = 3;//面交自提

    private Long orderId;//订单ID

    private String orderNo;//订单编号

    private Long productId;//商品ID

    private String productTitle;//商品标题

    private String productImage;//商品图片

    private Integer quantity;//购买数量

    private Long buyerId;

    private Long sellerId;

    private Integer deliveryMethod;//配送方式 1.快递 2.外卖跑腿 3.自提

    private String address;//地址

    private String logisticsCompany;//物流公司/同城跑腿平台

    private String logisticsNo;//物流单号/同城跑腿单号

    private BigDecimal unitPrice;//单价

    private BigDecimal amount;//商品金额

    private BigDecimal freight;//运费

    private BigDecimal totalAmount;//总金额

    private Integer status;//订单状态：1待确认 2待支付 3待发货 4待收货 5已收货 6取消订单

    private String cancelReason;//取消订单原因

    private String buyerMessage;//买家留言

    private String statusDesc;//状态描述

    private Integer payType;//支付方式

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;//订单过期时间

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;//创建时间

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime payTime;//支付时间

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime cancelTime;//取消订单时间

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime deliverTime;//发货时间

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime confirmTime;//确认订单时间

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime receiveTime;//确认收货时间


}
