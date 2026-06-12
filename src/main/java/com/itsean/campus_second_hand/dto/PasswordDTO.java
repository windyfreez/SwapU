package com.itsean.campus_second_hand.dto;

import lombok.Data;

@Data
public class PasswordDTO {

    private String oldPassword;//旧密码

    private String newPassword;//新密码

    private String confirmPassword;//确认密码
}
