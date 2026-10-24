package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品审核分页查询时传递的数据模型（管理端）
 */
@Data
@ApiModel(description = "商品审核分页查询时传递的数据模型")
public class ProductAuditPageQueryDTO implements Serializable {

    @ApiModelProperty(value = "页码，从1开始", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "审核状态：0待审核(默认) 1已通过 4已驳回", example = "0")
    private Integer status;

    @ApiModelProperty(value = "分类ID", example = "2")
    private Integer categoryId;

    @ApiModelProperty("商品标题（模糊查询）")
    private String title;

}
