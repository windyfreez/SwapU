package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 启用/禁用管理员时传递的数据模型
 */
@Data
@ApiModel(description = "启用或禁用管理员时传递的数据模型")
public class AdminStatusDTO implements Serializable {

    @ApiModelProperty("状态：1启用 0禁用")
    private Integer status;

}
