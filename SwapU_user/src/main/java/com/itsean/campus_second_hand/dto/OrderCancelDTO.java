package com.itsean.campus_second_hand.dto;

import lombok.Data;

@Data
public class OrderCancelDTO {

    private String orderNo;

    private String cancelReason;
}
