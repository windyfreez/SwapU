package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理员分页查询时传递的数据模型
 */
@Data
@ApiModel(description = "管理员分页查询时传递的数据模型")
public class AdminPageQueryDTO implements Serializable {

    @ApiModelProperty(value = "页码，从1开始", example = "1")
    private Integer pageNum;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize;

    @ApiModelProperty("账号（模糊查询）")
    private String username;

    @ApiModelProperty(value = "角色过滤：1超级管理员 2普通管理员", example = "1")
    private Integer role;

    @ApiModelProperty(value = "状态过滤：1启用 0禁用", example = "1")
    private Integer status;

}
