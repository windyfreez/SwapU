package com.itsean.swapu_admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品审核列表数据
 */
@Data
@ApiModel(description = "商品审核列表数据")
public class ProductAuditVO implements Serializable {

    @ApiModelProperty("商品ID")
    private Long id;

    @ApiModelProperty("商品标题")
    private String title;

    @ApiModelProperty("发布人ID")
    private Long userId;

    @ApiModelProperty("发布人账号")
    private String username;

    @ApiModelProperty("分类ID")
    private Integer categoryId;

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("售价")
    private BigDecimal price;

    @ApiModelProperty("原价")
    private BigDecimal originalPrice;

    @ApiModelProperty("图片列表")
    private List<String> images;

    @ApiModelProperty("成色")
    private Integer productCondition;

    @ApiModelProperty("审核状态：0待审核 1已通过 4已驳回")
    private Integer status;

    @ApiModelProperty("发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}
