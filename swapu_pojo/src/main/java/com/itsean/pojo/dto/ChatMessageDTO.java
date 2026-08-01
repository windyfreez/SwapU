package com.itsean.pojo.dto;

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
