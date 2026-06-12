package com.itsean.campus_second_hand.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDTO implements Serializable {

    private String username;

    private String phone;

    private String nickname;

    private String email;

    private String avatar;

    private String college;
}
