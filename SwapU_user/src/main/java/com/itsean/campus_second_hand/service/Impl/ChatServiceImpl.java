package com.itsean.campus_second_hand.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itsean.campus_second_hand.constant.MessageConstant;
import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.dto.ChatHistoryQueryDTO;
import com.itsean.campus_second_hand.dto.ChatMessageDTO;
import com.itsean.campus_second_hand.dto.ChatSessionQueryDTO;
import com.itsean.campus_second_hand.entity.ChatMessage;
import com.itsean.campus_second_hand.exception.ChatMessageException;
import com.itsean.campus_second_hand.mapper.ChatMapper;
import com.itsean.campus_second_hand.service.ChatService;
import com.itsean.campus_second_hand.vo.ChatMessageVO;
import com.itsean.campus_second_hand.vo.ChatSessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMapper chatMapper;

    /**
     * 发送消息
     * @param chatMessageDTO
     * @return
     */
    @Override
    public ChatMessage sendMessage(ChatMessageDTO chatMessageDTO) {
        Long currentUserId = BaseContext.getCurrentId();

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFromUserId(currentUserId);
        chatMessage.setToUserId(chatMessageDTO.getToUserId());
        //异常检验，无法给自己发消息
        if(chatMessage.getFromUserId() == chatMessage.getToUserId()){
            throw new ChatMessageException(MessageConstant.CANT_SEND_MESSAGE_TO_YOURSELF);
        }
        chatMessage.setProductId(chatMessageDTO.getProductId());
        chatMessage.setMessageType(chatMessageDTO.getMessageType() != null ? chatMessageDTO.getMessageType() : 1);
        chatMessage.setMessage(chatMessageDTO.getContent());
        chatMessage.setIsRead(0);
        chatMessage.setCreateTime(LocalDateTime.now());

        chatMapper.insertMessage(chatMessage);

        log.info("用户{}发送消息给用户{}", currentUserId, chatMessageDTO.getToUserId());
        return chatMessage;
    }

    /**
     * 获取聊天记录
     * @param queryDTO
     * @return
     */
    @Override
    public PageInfo<ChatMessageVO> getChatHistory(ChatHistoryQueryDTO queryDTO) {
        Long currentUserId = BaseContext.getCurrentId();

        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        List<ChatMessageVO> list = chatMapper.getChatHistory(currentUserId, queryDTO.getToUserId(), queryDTO.getProductId());

        return new PageInfo<>(list);
    }

    /**
     * 获取会话列表
     * @param queryDTO
     * @return
     */
    @Override
    public PageInfo<ChatSessionVO> getChatSessions(ChatSessionQueryDTO queryDTO) {
        Long currentUserId = BaseContext.getCurrentId();

        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        List<ChatSessionVO> list = chatMapper.getChatSessions(currentUserId);

        return new PageInfo<>(list);
    }

    /**
     * 标记消息已读
     * @param fromUserId
     */
    @Override
    public void markMessagesAsRead(Long fromUserId) {
        Long currentUserId = BaseContext.getCurrentId();
        chatMapper.markMessagesAsRead(currentUserId, fromUserId);
        log.info("用户{}标记来自用户{}的消息为已读", currentUserId, fromUserId);
    }
}
