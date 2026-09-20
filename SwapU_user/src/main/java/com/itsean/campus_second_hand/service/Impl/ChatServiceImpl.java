package com.itsean.campus_second_hand.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itsean.common.constant.MessageConstant;
import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.pojo.dto.ChatHistoryQueryDTO;
import com.itsean.pojo.dto.ChatMessageDTO;
import com.itsean.pojo.dto.ChatSessionQueryDTO;
import com.itsean.pojo.entity.ChatMessage;
import com.itsean.common.exception.ChatMessageException;
import com.itsean.campus_second_hand.handler.ChatWebSocketHandler;
import com.itsean.campus_second_hand.mapper.ChatMapper;
import com.itsean.campus_second_hand.service.ChatService;
import com.itsean.pojo.vo.ChatMessageVO;
import com.itsean.pojo.vo.ChatResponseVO;
import com.itsean.pojo.vo.ChatSessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
        //异常检验，无法给自己发消息（用 Objects.equals 做值比较，避免 Long 引用比较失效）
        if (Objects.equals(chatMessage.getFromUserId(), chatMessage.getToUserId())){
            throw new ChatMessageException(MessageConstant.CANT_SEND_MESSAGE_TO_YOURSELF);
        }
        chatMessage.setProductId(chatMessageDTO.getProductId());
        chatMessage.setMessageType(chatMessageDTO.getMessageType() != null ? chatMessageDTO.getMessageType() : 1);
        chatMessage.setMessage(chatMessageDTO.getContent());
        chatMessage.setIsRead(0);
        chatMessage.setCreateTime(LocalDateTime.now());

        chatMapper.insertMessage(chatMessage);

        //落库成功后实时推送给接收方，对方不在线时静默忽略（消息已入库，上线后仍可查到）
        ChatWebSocketHandler.sendToUser(chatMessage.getToUserId(), ChatResponseVO.fromMessage(chatMessage));

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

    /**
     * 系统发提醒消息
     * @param chatMessageDTO
     */
    @Override
    public void systemSendMessage(ChatMessageDTO chatMessageDTO) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFromUserId(33L);
        chatMessage.setToUserId(chatMessageDTO.getToUserId());
        chatMessage.setProductId(chatMessageDTO.getProductId());
        chatMessage.setMessageType(chatMessageDTO.getMessageType() != null ? chatMessageDTO.getMessageType() : 1);
        chatMessage.setMessage(chatMessageDTO.getContent());
        chatMessage.setIsRead(0);
        chatMessage.setCreateTime(LocalDateTime.now());
        chatMapper.insertMessage(chatMessage);

        //系统提醒同样实时推送，订单状态流转等通知无需刷新页面即可收到
        ChatWebSocketHandler.sendToUser(chatMessage.getToUserId(), ChatResponseVO.fromMessage(chatMessage));

    }
}
