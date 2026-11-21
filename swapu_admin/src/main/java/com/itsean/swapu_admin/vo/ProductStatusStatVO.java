package com.itsean.swapu_admin.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商品状态分布（饼图数据源）
 */
@Data
@ApiModel(description = "商品状态分布")
public class ProductStatusStatVO {

    @ApiModelProperty("商品状态：0审核中 1售卖中 2已售出 3已下架 4审核驳回")
    private Integer status;

    @ApiModelProperty("状态中文描述")
    private String statusDesc;

    @ApiModelProperty("数量")
    private Integer count;
}
