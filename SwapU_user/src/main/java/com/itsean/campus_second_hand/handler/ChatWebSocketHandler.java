package com.itsean.campus_second_hand.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsean.campus_second_hand.constant.JwtClaimsConstant;
import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.pojo.dto.ChatMessageDTO;
import com.itsean.pojo.entity.ChatMessage;
import com.itsean.campus_second_hand.properties.JwtProperties;
import com.itsean.campus_second_hand.service.ChatService;
import com.itsean.campus_second_hand.utils.JwtUtil;
import com.itsean.pojo.vo.ChatResponseVO;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天 WebSocket 端点：管理用户在线连接，并把消息实时推送给接收方
 */
@Component
@ServerEndpoint("/ws/chat/{userId}")
@Slf4j
public class ChatWebSocketHandler {

    /** 在线连接池：同一用户可能同时打开多个页面，因此一个 userId 对应一组 Session */
    private static final Map<Long, Set<Session>> sessionPool = new ConcurrentHashMap<>();

    /** Session 属性键：缓存 userId，断开连接时无需重复解析 token */
    private static final String SESSION_USER_ID = "userId";

    /** 使用 Spring Boot 自动配置的 ObjectMapper，其中已注册 JavaTimeModule，可序列化 LocalDateTime */
    private static ObjectMapper objectMapper;

    private static ChatService chatService;
    private static JwtProperties jwtProperties;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        ChatWebSocketHandler.objectMapper = objectMapper;
    }

    @Autowired
    public void setChatService(ChatService chatService) {
        ChatWebSocketHandler.chatService = chatService;
    }

    @Autowired
    public void setJwtProperties(JwtProperties jwtProperties) {
        ChatWebSocketHandler.jwtProperties = jwtProperties;
    }

    /**
     * 建立连接：校验 token 通过后把当前 Session 放入在线连接池
     * @param session 当前连接
     * @param userIdParam 路径参数中的用户 id
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdParam) {
        try {
            Long userId = resolveUserId(session);
            if (userId == null) {
                log.warn("WebSocket连接失败：token缺失或无效，pathUserId: {}", userIdParam);
                session.close();
                return;
            }

            session.getUserProperties().put(SESSION_USER_ID, userId);
            sessionPool.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
            log.info("用户{}建立WebSocket连接，sessionId: {}", userId, session.getId());
        } catch (Exception e) {
            log.error("WebSocket连接异常: {}", e.getMessage());
            closeQuietly(session);
            return;
        }

        // 欢迎语只是提示，发送失败不能连累已经建好的连接（否则客户端会陷入重连死循环）
        try {
            sendSuccessMessage(session, "连接成功");
        } catch (Exception e) {
            log.warn("连接成功提示发送失败，sessionId: {}，原因: {}", session.getId(), e.getMessage());
        }
    }

    /**
     * 接收客户端通过 WebSocket 发送的消息：落库后由 Service 推送给接收方，再把结果回显给发送方
     * @param session 当前连接
     * @param message 客户端消息体（ChatMessageDTO 的 JSON）
     * @param userIdParam 路径参数中的用户 id
     */
    @OnMessage
    public void onMessage(Session session, String message, @PathParam("userId") String userIdParam) {
        try {
            Long fromUserId = (Long) session.getUserProperties().get(SESSION_USER_ID);
            if (fromUserId == null) {
                fromUserId = resolveUserId(session);
            }
            if (fromUserId == null) {
                log.warn("WebSocket消息处理失败：连接未通过token校验");
                return;
            }

            ChatMessageDTO chatMessageDTO = objectMapper.readValue(message, ChatMessageDTO.class);

            // WebSocket 线程没有经过 JwtTokenUserInterceptor，需要手动把当前用户写入上下文，
            // 否则 Service 里 BaseContext.getCurrentId() 取不到值，消息的发送人会为空
            BaseContext.setCurrentId(fromUserId);
            ChatMessage chatMessage;
            try {
                chatMessage = chatService.sendMessage(chatMessageDTO);
            } finally {
                BaseContext.removeCurrentId();
            }

            // 接收方的实时推送已由 ChatServiceImpl.sendMessage 统一处理，这里只回显给发送方
            session.getBasicRemote().sendText(objectMapper.writeValueAsString(ChatResponseVO.fromMessage(chatMessage)));
        } catch (Exception e) {
            log.error("处理WebSocket消息异常: {}", e.getMessage());
        }
    }

    /**
     * 关闭连接：把当前 Session 从在线连接池移除
     * @param session 当前连接
     * @param userIdParam 路径参数中的用户 id
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") String userIdParam) {
        try {
            Long userId = (Long) session.getUserProperties().get(SESSION_USER_ID);
            if (userId == null) {
                userId = resolveUserId(session);
            }
            if (userId != null) {
                removeSession(userId, session);
                log.info("用户{}断开WebSocket连接，sessionId: {}", userId, session.getId());
            }
        } catch (Exception e) {
            log.error("WebSocket关闭异常: {}", e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误: {}", error.getMessage());
    }

    /**
     * 向指定用户实时推送聊天消息（用户不在线时直接忽略，消息已落库不会丢失）
     * @param toUserId 接收方用户 id
     * @param responseVO 推送内容
     * @return 是否至少推送到了一个在线连接
     */
    public static boolean sendToUser(Long toUserId, ChatResponseVO responseVO) {
        if (toUserId == null || responseVO == null) {
            return false;
        }

        Set<Session> sessions = sessionPool.get(toUserId);
        if (sessions == null || sessions.isEmpty()) {
            log.warn("用户{}不在线，消息已保存，等待其上线后查询", toUserId);
            return false;
        }

        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(responseVO);
        } catch (Exception e) {
            log.error("聊天消息序列化失败: {}", e.getMessage());
            return false;
        }

        boolean pushed = false;
        for (Session session : sessions) {
            if (session == null || !session.isOpen()) {
                continue;
            }
            try {
                session.getBasicRemote().sendText(jsonMessage);
                pushed = true;
            } catch (IOException e) {
                // 单个连接发送失败不影响其他页面，顺手清理失效连接
                log.error("推送给用户{}失败，sessionId: {}，原因: {}", toUserId, session.getId(), e.getMessage());
                removeSession(toUserId, session);
            }
        }

        if (pushed) {
            log.info("推送消息给用户{}，messageId: {}", toUserId, responseVO.getMessageId());
        }
        return pushed;
    }

    /**
     * 把 Session 从在线连接池移除，连接全部断开时清理该用户的条目
     * @param userId 用户 id
     * @param session 待移除的连接
     */
    private static void removeSession(Long userId, Session session) {
        Set<Session> sessions = sessionPool.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        // 只有最后一个页面也断开时才移除用户条目，避免多标签页互相顶掉
        sessionPool.computeIfPresent(userId, (key, value) -> value.isEmpty() ? null : value);
    }

    /**
     * 解析连接携带的 token，取出用户 id
     * @param session 当前连接
     * @return 用户 id，token 缺失或无效时返回 null
     */
    private Long resolveUserId(Session session) {
        String token = getTokenFromSession(session);
        if (token == null) {
            return null;
        }
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            return Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
        } catch (Exception e) {
            log.warn("WebSocket token解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从连接地址的查询参数中取出 token（兼容 token 之后还有其他参数的情况）
     * @param session 当前连接
     * @return token，未携带时返回 null
     */
    private String getTokenFromSession(Session session) {
        Map<String, List<String>> parameterMap = session.getRequestParameterMap();
        if (parameterMap != null) {
            List<String> tokens = parameterMap.get("token");
            if (tokens != null && !tokens.isEmpty()) {
                String token = tokens.get(0);
                if (token != null && !token.trim().isEmpty()) {
                    return token.trim();
                }
            }
        }

        // 兜底：个别容器取不到参数表时，退回解析原始查询串
        String queryString = session.getQueryString();
        if (queryString == null) {
            return null;
        }
        for (String pair : queryString.split("&")) {
            if (pair.startsWith("token=")) {
                return URLDecoder.decode(pair.substring("token=".length()), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    /**
     * 关闭连接，忽略关闭过程中的异常
     * @param session 待关闭的连接
     */
    private void closeQuietly(Session session) {
        try {
            session.close();
        } catch (IOException e) {
            log.error("关闭session异常: {}", e.getMessage());
        }
    }

    /**
     * 向刚建立连接的客户端回一条连接成功提示
     * @param session 当前连接
     * @param message 提示文案
     */
    private void sendSuccessMessage(Session session, String message) throws IOException {
        ChatResponseVO responseVO = new ChatResponseVO();
        responseVO.setCode(200);
        responseVO.setContent(message);
        responseVO.setCreateTime(LocalDateTime.now());
        session.getBasicRemote().sendText(objectMapper.writeValueAsString(responseVO));
    }
}
