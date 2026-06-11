package com.itsean.campus_second_hand.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderPageQueryDTO {
    public static final String I_AM_BUYER = "buyer";
    public static final String I_AM_SELLER = "seller";

    private Integer page;

    private Integer pageSize;

    private Integer status;

    private String role;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long currentId;

}
