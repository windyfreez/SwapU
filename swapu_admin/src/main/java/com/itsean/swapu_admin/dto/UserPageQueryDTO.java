package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户分页查询时传递的数据模型（管理端）
 */
@Data
@ApiModel(description = "用户分页查询时传递的数据模型")
public class UserPageQueryDTO implements Serializable {

    @ApiModelProperty(value = "页码，从1开始", example = "1")
    private Integer pageNum;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize;

    @ApiModelProperty("用户名/昵称（模糊查询）")
    private String keyword;

    @ApiModelProperty("学号（模糊查询）")
    private String studentId;

    @ApiModelProperty("手机号（模糊查询）")
    private String phone;

    @ApiModelProperty(value = "状态过滤：1启用 0禁用", example = "1")
    private Integer status;

}
