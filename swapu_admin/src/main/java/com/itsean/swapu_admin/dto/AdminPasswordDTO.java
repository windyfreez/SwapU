package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理员修改自己密码时传递的数据模型
 */
@Data
@ApiModel(description = "管理员修改自己密码时传递的数据模型")
public class AdminPasswordDTO implements Serializable {

    @ApiModelProperty("原密码")
    private String oldPassword;

    @ApiModelProperty("新密码（6-20位）")
    private String newPassword;

}
