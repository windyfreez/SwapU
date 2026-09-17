package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 启用/禁用分类时传递的数据模型
 */
@Data
@ApiModel(description = "启用或禁用分类时传递的数据模型")
public class CategoryStatusDTO implements Serializable {

    @ApiModelProperty(value = "状态：1启用 0禁用", example = "1")
    private Integer status;

}
