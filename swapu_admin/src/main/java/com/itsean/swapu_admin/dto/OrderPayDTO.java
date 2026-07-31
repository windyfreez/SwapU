package com.itsean.swapu_admin.dto;

import lombok.Data;

@Data
public class OrderPayDTO {
    private String orderNo;

    private Integer payType;

    private String payPassword;
}
