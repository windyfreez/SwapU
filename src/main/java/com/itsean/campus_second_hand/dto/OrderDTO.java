package com.itsean.campus_second_hand.dto;

import lombok.Data;

@Data
public class OrderDTO {

    private Long productId;

    private String address;

    private Integer deliveryMethod;

    private Integer quantity;

    private String buyerMessage;
}
