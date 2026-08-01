package com.itsean.pojo.dto;

import lombok.Data;

@Data
public class OrderCancelDTO {

    private String orderNo;

    private String cancelReason;
}
