package com.itsean.swapu_admin.mapper;

import com.itsean.swapu_admin.entity.ChatMessage;
import com.itsean.swapu_admin.vo.ChatMessageVO;
import com.itsean.swapu_admin.vo.ChatSessionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMapper {

    void insertMessage(ChatMessage chatMessage);

    List<ChatMessageVO> getChatHistory(@Param("currentUserId") Long currentUserId,
                                       @Param("toUserId") Long toUserId,
                                       @Param("productId") Long productId);

    List<ChatSessionVO> getChatSessions(Long currentUserId);

    void markMessagesAsRead(@Param("currentUserId") Long currentUserId,
                            @Param("fromUserId") Long fromUserId);

    int getUnreadCount(@Param("currentUserId") Long currentUserId,
                       @Param("fromUserId") Long fromUserId);
}
