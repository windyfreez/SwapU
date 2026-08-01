package com.itsean.pojo.dto;

import lombok.Data;

@Data
public class OrderDTO {

    private Long productId;

    private Long addressId;

    private Integer deliveryMethod;

    private Integer quantity;

    private String buyerMessage;
}
