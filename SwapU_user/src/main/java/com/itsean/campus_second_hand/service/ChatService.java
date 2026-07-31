package com.itsean.campus_second_hand.service;

import com.github.pagehelper.PageInfo;
import com.itsean.campus_second_hand.dto.ChatHistoryQueryDTO;
import com.itsean.campus_second_hand.dto.ChatMessageDTO;
import com.itsean.campus_second_hand.dto.ChatSessionQueryDTO;
import com.itsean.campus_second_hand.entity.ChatMessage;
import com.itsean.campus_second_hand.vo.ChatMessageVO;
import com.itsean.campus_second_hand.vo.ChatSessionVO;

public interface ChatService {

    /**
     * 发送消息
     * @param chatMessageDTO
     * @return
     */
    ChatMessage sendMessage(ChatMessageDTO chatMessageDTO);

    /**
     * 获取聊天记录
     * @param queryDTO
     * @return
     */
    PageInfo<ChatMessageVO> getChatHistory(ChatHistoryQueryDTO queryDTO);

    /**
     * 获取会话列表
     * @param queryDTO
     * @return
     */
    PageInfo<ChatSessionVO> getChatSessions(ChatSessionQueryDTO queryDTO);

    /**
     * 标记消息已读
     * @param fromUserId
     */
    void markMessagesAsRead(Long fromUserId);
}
