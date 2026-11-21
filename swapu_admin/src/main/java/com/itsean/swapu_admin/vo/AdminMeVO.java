package com.itsean.swapu_admin.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 当前登录管理员信息（/admin/me）
 * <p>
 * 前端用它作为菜单与路由守卫的唯一数据源，避免在前端硬编码「角色 === 1」这类判断。
 */
@Data
@ApiModel(description = "当前登录管理员信息与权限")
public class AdminMeVO {

    @ApiModelProperty("管理员ID")
    private Long id;

    @ApiModelProperty("登录账号")
    private String username;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("头像URL")
    private String avatar;

    @ApiModelProperty("角色：1超级管理员 2普通管理员 3运营管理员")
    private Integer role;

    @ApiModelProperty("角色名称")
    private String roleLabel;

    @ApiModelProperty("权限点列表，如 AUDIT / USER_MANAGE")
    private List<String> permissions;
}
