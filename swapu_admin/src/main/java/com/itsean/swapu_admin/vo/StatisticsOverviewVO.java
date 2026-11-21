package com.itsean.swapu_admin.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 平台总览统计（工作台首页卡片）
 */
@Data
@ApiModel(description = "平台总览统计")
public class StatisticsOverviewVO {

    @ApiModelProperty("在售商品数（product.status = 1）")
    private Integer onSaleCount;

    @ApiModelProperty("待审核商品数（product.status = 0）")
    private Integer pendingAuditCount;

    @ApiModelProperty("交易完成数（orders.status = 5 已收货）")
    private Integer soldCount;

    @ApiModelProperty("累计成交总额（已完成订单）")
    private BigDecimal totalAmount;

    @ApiModelProperty("平台商品总浏览量（product.view_count 之和）")
    private Long totalViewCount;

    @ApiModelProperty("注册用户总数")
    private Integer totalUserCount;

    @ApiModelProperty("今日新增订单数")
    private Integer todayOrderCount;

    @ApiModelProperty("今日成交额（按今日完成的订单）")
    private BigDecimal todayAmount;
}
