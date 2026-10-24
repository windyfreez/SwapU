package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 评论审核分页查询时传递的数据模型（管理端）
 */
@Data
@ApiModel(description = "评论审核分页查询时传递的数据模型")
public class CommentAuditPageQueryDTO implements Serializable {

    @ApiModelProperty(value = "页码，从1开始", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "审核状态：0待审核(默认) 1已发布 2已驳回", example = "0")
    private Integer status;

    @ApiModelProperty(value = "按被评论人ID过滤（receiver_id）", example = "3")
    private Long receiverId;

    @ApiModelProperty("评论内容关键字")
    private String keyword;

}
