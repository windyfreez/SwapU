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
}
