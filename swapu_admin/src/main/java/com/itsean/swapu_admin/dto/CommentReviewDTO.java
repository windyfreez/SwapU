package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 评论审核（通过/驳回）请求参数
 */
@Data
@ApiModel(description = "评论审核请求参数")
public class CommentReviewDTO implements Serializable {

    @ApiModelProperty(value = "评论ID", required = true, example = "9")
    private Long commentId;

    @ApiModelProperty(value = "审核结论：1通过 2驳回", required = true, example = "2")
    private Integer result;

    @ApiModelProperty(value = "驳回原因，驳回时必填", example = "评论内容包含不文明用语")
    private String reason;

}
