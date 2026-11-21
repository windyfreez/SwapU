package com.itsean.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderStatisticsVO {
    /**
     * 待支付订单数（买家视角）
     */
    private Integer waitPay;

    /**
     * 待收货订单数（买家视角）
     */
    private Integer waitReceive;

    /**
     * 已收货订单数（买家视角）
     */
    private Integer buyCompleted;

    private BigDecimal totalCostAmount;

    /**
     * 待发货订单数（卖家视角）
     */
    private Integer waitDeliver;

    /**
     * 已收货订单数（卖家视角）
     */
    private Integer sellCompleted;

    private BigDecimal totalSellAmount;

    /**
     * 待确认订单数（卖家视角：买家已下单，等待我确认接单）
     */
    private Integer waitAccept;

    /**
     * 退货审核中（卖家视角：买家已申请退货，等待我审核）
     */
    private Integer refundApplying;

    /**
     * 取消申请中（卖家视角：买家申请取消待支付订单，等待我审核）
     */
    private Integer cancelApplying;

    /** 在售商品数 */
    private Integer publishCount;

    /** 已售商品数 */
    private Integer soldCount;

    /** 我的收藏数 */
    private Integer favoriteCount;

    /** 足迹(浏览记录)总数 */
    private Integer footprintCount;
}
