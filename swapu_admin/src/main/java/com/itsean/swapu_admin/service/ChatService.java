package com.itsean.swapu_admin.service;

import com.github.pagehelper.PageInfo;
import com.itsean.swapu_admin.dto.ChatHistoryQueryDTO;
import com.itsean.swapu_admin.dto.ChatMessageDTO;
import com.itsean.swapu_admin.dto.ChatSessionQueryDTO;
import com.itsean.swapu_admin.entity.ChatMessage;
import com.itsean.swapu_admin.vo.ChatMessageVO;
import com.itsean.swapu_admin.vo.ChatSessionVO;

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
