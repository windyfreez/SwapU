package com.itsean.campus_second_hand.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteDTO {

    private Long id;

    private Long userId;

    private Long productId;

    private LocalDateTime createTime;
}
