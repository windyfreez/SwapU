package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品审核（通过/驳回）请求参数
 */
@Data
@ApiModel(description = "商品审核请求参数")
public class ProductReviewDTO implements Serializable {

    @ApiModelProperty(value = "商品ID", required = true, example = "10")
    private Long productId;

    @ApiModelProperty(value = "审核结论：1通过 2驳回", required = true, example = "2")
    private Integer result;

    @ApiModelProperty(value = "驳回原因，驳回时必填", example = "商品描述与图片不符，请修改后重新提交")
    private String reason;

}
