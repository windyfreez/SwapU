package com.itsean.swapu_admin.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户交易统计（管理端），按买家/卖家两个视角统计
 */
@Data
@Builder
@ApiModel(description = "用户交易统计")
public class UserTradeStatisticsVO implements Serializable {

    //作为买家
    @ApiModelProperty("待支付订单数（买家）")
    private Integer buyWaitPay;

    @ApiModelProperty("待收货订单数（买家）")
    private Integer buyWaitReceive;

    @ApiModelProperty("已成交订单数（买家）")
    private Integer buyCompleted;

    @ApiModelProperty("已取消订单数（买家）")
    private Integer buyCanceled;

    @ApiModelProperty("累计买入金额（已收货订单）")
    private BigDecimal buyTotalAmount;

    //作为卖家
    @ApiModelProperty("待发货订单数（卖家）")
    private Integer sellWaitDeliver;

    @ApiModelProperty("已成交订单数（卖家）")
    private Integer sellCompleted;

    @ApiModelProperty("已取消订单数（卖家）")
    private Integer sellCanceled;

    @ApiModelProperty("累计卖出金额（已收货订单）")
    private BigDecimal sellTotalAmount;

}
