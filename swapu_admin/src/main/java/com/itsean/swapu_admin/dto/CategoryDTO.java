package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增、修改分类时传递的数据模型（新增时 id 为空，修改时 id 必填）
 */
@Data
@ApiModel(description = "新增或修改分类时传递的数据模型")
public class CategoryDTO implements Serializable {

    @ApiModelProperty(value = "分类ID，修改时必填", example = "1")
    private Long id;

    @ApiModelProperty("分类名称")
    private String name;

    @ApiModelProperty(value = "排序值，越小越靠前，默认0", example = "1")
    private Integer sort;

}
