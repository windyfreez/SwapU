package com.itsean.campus_second_hand.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话中的单条消息，对应大模型 messages 数组的一项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMessageDTO {

    /**
     * 角色：system / user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;
}
