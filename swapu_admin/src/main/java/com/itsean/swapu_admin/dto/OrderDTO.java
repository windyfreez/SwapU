package com.itsean.swapu_admin.dto;

import lombok.Data;

@Data
public class OrderDTO {

    private Long productId;

    private Long addressId;

    private Integer deliveryMethod;

    private Integer quantity;

    private String buyerMessage;
}
