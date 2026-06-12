package com.itsean.campus_second_hand.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {

    private Long toUserId;

    private Long productId;

    private Integer messageType;

    private String content;
}
