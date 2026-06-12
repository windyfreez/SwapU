package com.itsean.campus_second_hand.dto;

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
}
