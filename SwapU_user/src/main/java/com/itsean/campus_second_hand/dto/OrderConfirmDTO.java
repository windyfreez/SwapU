package com.itsean.campus_second_hand.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderConfirmDTO {
    private String orderNo;

    private Integer status;

    private BigDecimal freight;
}
