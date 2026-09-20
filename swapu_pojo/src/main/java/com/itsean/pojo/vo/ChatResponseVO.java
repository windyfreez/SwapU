package com.itsean.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itsean.pojo.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseVO {

    private Integer code;

    private Long messageId;

    private Long fromUserId;

    private Long toUserId;

    private Long productId;

    private Integer messageType;

    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 由落库后的聊天消息构建响应对象（HTTP 返回与 WebSocket 实时推送共用同一份数据）
     * @param chatMessage 已插入数据库的聊天消息
     * @return 聊天响应对象
     */
    public static ChatResponseVO fromMessage(ChatMessage chatMessage) {
        ChatResponseVO responseVO = new ChatResponseVO();
        responseVO.setCode(200);
        responseVO.setMessageId(chatMessage.getId());
        responseVO.setFromUserId(chatMessage.getFromUserId());
        responseVO.setToUserId(chatMessage.getToUserId());
        responseVO.setProductId(chatMessage.getProductId());
        responseVO.setMessageType(chatMessage.getMessageType());
        responseVO.setContent(chatMessage.getMessage());
        responseVO.setCreateTime(chatMessage.getCreateTime());
        return responseVO;
    }
}
