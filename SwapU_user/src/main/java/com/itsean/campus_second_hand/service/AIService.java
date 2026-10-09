package com.itsean.campus_second_hand.service;

import com.itsean.campus_second_hand.dto.AiChatDTO;
import com.itsean.campus_second_hand.vo.AiChatVO;

/**
 * AI 客服服务
 */
public interface AIService {

    /**
     * 向大模型发起一次客服问答
     * @param aiChatDTO 提问参数
     * @return AI 回答
     */
    AiChatVO chat(AiChatDTO aiChatDTO);
}
