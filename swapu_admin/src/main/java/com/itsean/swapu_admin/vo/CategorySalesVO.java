package com.itsean.swapu_admin.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 分类销售分布（饼图/柱状图数据源，取成交额 Top N）
 */
@Data
@ApiModel(description = "分类销售分布")
public class CategorySalesVO {

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("成交件数（订单购买数量之和）")
    private Long salesCount;

    @ApiModelProperty("成交金额")
    private BigDecimal salesAmount;
}
