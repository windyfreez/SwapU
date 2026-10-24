package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 审核流水分页查询时传递的数据模型（管理端）
 */
@Data
@ApiModel(description = "审核流水分页查询时传递的数据模型")
public class AuditRecordPageQueryDTO implements Serializable {

    @ApiModelProperty(value = "页码，从1开始", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "审核类型：1商品 2评论 3头像", example = "1")
    private Integer type;

    @ApiModelProperty(value = "审核结论：1通过 2驳回", example = "2")
    private Integer result;

    @ApiModelProperty(value = "审核人ID", example = "1")
    private Long reviewerId;

}
