package com.itsean.swapu_admin.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理员基础信息（登录返回、列表返回共用，不含密码等敏感字段）
 */
@Data
@ApiModel(description = "管理员基础信息")
public class AdminInfoVO implements Serializable {

    @ApiModelProperty("管理员ID")
    private Long id;

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("头像URL")
    private String avatar;

    @ApiModelProperty("角色：1超级 2普通")
    private Integer role;

}
