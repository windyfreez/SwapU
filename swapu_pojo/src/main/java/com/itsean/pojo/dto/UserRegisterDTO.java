package com.itsean.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDTO {

    private String studentId;

    private String username;

    private String nickname;

    private String password;

    private String phone;

    private String email;

    private String college;

    private String smsCode;

    /**
     * 图形验证码标识，取自 /captcha/image?scene=user-register 的返回值
     */
    private String captchaKey;

    /**
     * 用户输入的图形验证码
     */
    private String captchaCode;
}
