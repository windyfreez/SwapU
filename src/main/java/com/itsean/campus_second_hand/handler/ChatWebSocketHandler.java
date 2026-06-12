package com.itsean.campus_second_hand.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsean.campus_second_hand.dto.ChatMessageDTO;
import com.itsean.campus_second_hand.entity.ChatMessage;
import com.itsean.campus_second_hand.properties.JwtProperties;
import com.itsean.campus_second_hand.service.ChatService;
import com.itsean.campus_second_hand.utils.JwtUtil;
import com.itsean.campus_second_hand.vo.ChatResponseVO;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/chat/{userId}")
@Slf4j
public class ChatWebSocketHandler {

    private static ConcurrentHashMap<Long, Session> sessionPool = new ConcurrentHashMap<>();

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static ChatService chatService;
    private static JwtProperties jwtProperties;

    @Autowired
    public void setChatService(ChatService chatService) {
        ChatWebSocketHandler.chatService = chatService;
    }

    @Autowired
    public void setJwtProperties(JwtProperties jwtProperties) {
        ChatWebSocketHandler.jwtProperties = jwtProperties;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdParam) {
        try {
            String token = getTokenFromSession(session);
            if (token == null || token.isEmpty()) {
                log.warn("WebSocket连接失败：缺少token");
                session.close();
                return;
            }

            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(com.itsean.campus_second_hand.constant.JwtClaimsConstant.USER_ID).toString());

            sessionPool.put(userId, session);
            log.info("用户{}建立WebSocket连接，sessionId: {}", userId, session.getId());

            sendSuccessMessage(session, "连接成功");
        } catch (Exception e) {
            log.error("WebSocket连接异常: {}", e.getMessage());
            try {
                session.close();
            } catch (IOException ex) {
                log.error("关闭session异常: {}", ex.getMessage());
            }
        }
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("userId") String userIdParam) {
        try {
            String token = getTokenFromSession(session);
            if (token == null || token.isEmpty()) {
                log.warn("WebSocket消息处理失败：缺少token");
                return;
            }

            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long fromUserId = Long.valueOf(claims.get(com.itsean.campus_second_hand.constant.JwtClaimsConstant.USER_ID).toString());

            ChatMessageDTO chatMessageDTO = objectMapper.readValue(message, ChatMessageDTO.class);

            ChatMessage chatMessage = chatService.sendMessage(chatMessageDTO);

            Session toSession = sessionPool.get(chatMessageDTO.getToUserId());
            if (toSession != null && toSession.isOpen()) {
                ChatResponseVO responseVO = new ChatResponseVO();
                responseVO.setCode(200);
                responseVO.setMessageId(chatMessage.getId());
                responseVO.setFromUserId(chatMessage.getFromUserId());
                responseVO.setToUserId(chatMessage.getToUserId());
                responseVO.setProductId(chatMessage.getProductId());
                responseVO.setMessageType(chatMessage.getMessageType());
                responseVO.setContent(chatMessage.getMessage());
                responseVO.setCreateTime(chatMessage.getCreateTime());

                String jsonMessage = objectMapper.writeValueAsString(responseVO);
                toSession.getBasicRemote().sendText(jsonMessage);
                log.info("推送消息给用户{}", chatMessageDTO.getToUserId());
            } else {
                log.warn("用户{}不在线，消息已保存", chatMessageDTO.getToUserId());
            }

            ChatResponseVO senderResponse = new ChatResponseVO();
            senderResponse.setCode(200);
            senderResponse.setMessageId(chatMessage.getId());
            senderResponse.setFromUserId(chatMessage.getFromUserId());
            senderResponse.setToUserId(chatMessage.getToUserId());
            senderResponse.setProductId(chatMessage.getProductId());
            senderResponse.setMessageType(chatMessage.getMessageType());
            senderResponse.setContent(chatMessage.getMessage());
            senderResponse.setCreateTime(chatMessage.getCreateTime());

            session.getBasicRemote().sendText(objectMapper.writeValueAsString(senderResponse));

        } catch (Exception e) {
            log.error("处理WebSocket消息异常: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userIdParam) {
        try {
            String token = getTokenFromSession(session);
            if (token != null && !token.isEmpty()) {
                Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
                Long userId = Long.valueOf(claims.get(com.itsean.campus_second_hand.constant.JwtClaimsConstant.USER_ID).toString());

                sessionPool.remove(userId);
                log.info("用户{}断开WebSocket连接", userId);
            }
        } catch (Exception e) {
            log.error("WebSocket关闭异常: {}", e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误: {}", error.getMessage());
        error.printStackTrace();
    }

    private String getTokenFromSession(Session session) {
        String queryString = session.getQueryString();
        if (queryString != null && queryString.contains("token=")) {
            return queryString.split("token=")[1];
        }
        return null;
    }

    private void sendSuccessMessage(Session session, String message) throws IOException {
        ChatResponseVO responseVO = new ChatResponseVO();
        responseVO.setCode(200);
        responseVO.setContent(message);
        responseVO.setCreateTime(LocalDateTime.now());
        session.getBasicRemote().sendText(objectMapper.writeValueAsString(responseVO));
    }
}
