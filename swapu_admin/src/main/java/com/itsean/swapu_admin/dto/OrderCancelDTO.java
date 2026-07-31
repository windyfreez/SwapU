package com.itsean.swapu_admin.dto;

import lombok.Data;

@Data
public class OrderCancelDTO {

    private String orderNo;

    private String cancelReason;
}
