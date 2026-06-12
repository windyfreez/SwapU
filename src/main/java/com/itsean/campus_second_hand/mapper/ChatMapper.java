package com.itsean.campus_second_hand.mapper;

import com.itsean.campus_second_hand.entity.ChatMessage;
import com.itsean.campus_second_hand.vo.ChatMessageVO;
import com.itsean.campus_second_hand.vo.ChatSessionVO;
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
