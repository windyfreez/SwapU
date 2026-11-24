package com.itsean.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "员工登录时传递的数据模型")
public class UserLoginDTO implements Serializable {

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("图形验证码标识，取自 /captcha/image 的返回值")
    private String captchaKey;

    @ApiModelProperty("用户输入的图形验证码")
    private String captchaCode;

}
