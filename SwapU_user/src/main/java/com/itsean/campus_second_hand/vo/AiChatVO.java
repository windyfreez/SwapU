package com.itsean.campus_second_hand.vo;

import lombok.Data;

/**
 * AI 客服问答响应结果
 */
@Data
public class AiChatVO {

    /**
     * 回答正文
     */
    private String content;

    /**
     * 模型的思考过程，仅开启思考模式时有值
     */
    private String reasoningContent;

    /**
     * 本次实际使用的模型
     */
    private String model;

    /**
     * 本次消耗的总 token 数
     */
    private Integer totalTokens;

    /**
     * 结束原因：stop 表示正常结束，length 表示达到长度上限被截断
     */
    private String finishReason;
}
