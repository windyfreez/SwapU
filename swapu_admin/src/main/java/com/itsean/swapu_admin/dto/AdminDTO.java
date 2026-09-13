package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增、修改管理员时传递的数据模型（新增时 id 为空，修改时 id 必填）
 */
@Data
@ApiModel(description = "新增或修改管理员时传递的数据模型")
public class AdminDTO implements Serializable {

    @ApiModelProperty("管理员ID，修改时必填")
    private Long id;

    @ApiModelProperty("登录账号（新增时必填，2-20字符）")
    private String username;

    @ApiModelProperty("登录密码（新增时必填 6-20 位；修改时传入则重置密码）")
    private String password;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("头像URL")
    private String avatar;

    @ApiModelProperty("角色：1超级管理员 2普通管理员，默认2")
    private Integer role;

}
