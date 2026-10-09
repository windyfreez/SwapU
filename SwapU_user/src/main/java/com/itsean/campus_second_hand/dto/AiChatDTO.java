package com.itsean.campus_second_hand.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 客服问答请求参数
 */
@Data
public class AiChatDTO {

    /**
     * 用户本次提问
     */
    private String prompt;

    /**
     * 自定义人设，不传则使用默认的云市集客服人设
     */
    private String systemPrompt;

    /**
     * 历史对话，按时间正序排列，可为空
     */
    private List<AiMessageDTO> history;
}
