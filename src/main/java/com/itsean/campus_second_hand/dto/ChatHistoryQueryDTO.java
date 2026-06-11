package com.itsean.campus_second_hand.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryQueryDTO {

    private Long toUserId;

    private Long productId;

    private Integer page = 1;

    private Integer size = 20;
}
