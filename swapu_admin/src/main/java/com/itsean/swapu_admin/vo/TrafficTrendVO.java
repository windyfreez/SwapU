package com.itsean.swapu_admin.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 流量趋势的单日数据点（折线图数据源）
 */
@Data
@ApiModel(description = "流量趋势单日数据")
public class TrafficTrendVO {

    @ApiModelProperty("日期 yyyy-MM-dd")
    private String date;

    @ApiModelProperty("当日浏览量（登录用户的浏览行为数）")
    private Long viewCount;

    @ApiModelProperty("当日新增订单数")
    private Long orderCount;

    @ApiModelProperty("当日成交额（当日完成的订单）")
    private BigDecimal amount;
}
