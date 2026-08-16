package com.itsean.campus_second_hand.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderStatisticsVO {
    private Integer waitPay;

    private Integer waitReceive;

    private Integer buyCompleted;

    private BigDecimal totalCostAmount;

    private Integer waitDeliver;

    private Integer sellCompleted;

    private BigDecimal totalSellAmount;

    /** 在售商品数 */
    private Integer publishCount;

    /** 已售商品数 */
    private Integer soldCount;

    /** 我的收藏数 */
    private Integer favoriteCount;

    /** 足迹(浏览记录)总数 */
    private Integer footprintCount;
}
