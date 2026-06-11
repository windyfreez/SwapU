package com.itsean.campus_second_hand.controller.user;

import com.github.pagehelper.PageInfo;
import com.itsean.campus_second_hand.dto.ChatHistoryQueryDTO;
import com.itsean.campus_second_hand.dto.ChatMessageDTO;
import com.itsean.campus_second_hand.dto.ChatSessionQueryDTO;
import com.itsean.campus_second_hand.dto.MarkReadDTO;
import com.itsean.campus_second_hand.entity.ChatMessage;
import com.itsean.campus_second_hand.entity.result.Result;
import com.itsean.campus_second_hand.service.ChatService;
import com.itsean.campus_second_hand.vo.ChatMessageVO;
import com.itsean.campus_second_hand.vo.ChatResponseVO;
import com.itsean.campus_second_hand.vo.ChatSessionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ws/chat")
@Slf4j
@Api(tags = "聊天接口")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    @ApiOperation("发送消息")
    public Result<ChatResponseVO> sendMessage(@RequestBody ChatMessageDTO chatMessageDTO) {
        log.info("发送消息：{}", chatMessageDTO);
        ChatMessage chatMessage = chatService.sendMessage(chatMessageDTO);

        ChatResponseVO responseVO = new ChatResponseVO();
        responseVO.setCode(200);
        responseVO.setMessageId(chatMessage.getId());
        responseVO.setFromUserId(chatMessage.getFromUserId());
        responseVO.setToUserId(chatMessage.getToUserId());
        responseVO.setProductId(chatMessage.getProductId());
        responseVO.setMessageType(chatMessage.getMessageType());
        responseVO.setContent(chatMessage.getMessage());
        responseVO.setCreateTime(chatMessage.getCreateTime());

        return Result.success(responseVO);
    }

    /**
     * 获取聊天记录
     * @param queryDTO
     * @return
     */
    @GetMapping("/history")
    @ApiOperation("获取聊天记录")
    public Result<PageInfo<ChatMessageVO>> getChatHistory(ChatHistoryQueryDTO queryDTO) {
        log.info("获取聊天记录：{}", queryDTO);
        PageInfo<ChatMessageVO> pageInfo = chatService.getChatHistory(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取会话列表
     * @param queryDTO
     * @return
     */
    @GetMapping("/sessions")
    @ApiOperation("获取会话列表")
    public Result<PageInfo<ChatSessionVO>> getChatSessions(ChatSessionQueryDTO queryDTO) {
        log.info("获取会话列表：{}", queryDTO);
        PageInfo<ChatSessionVO> pageInfo = chatService.getChatSessions(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 标记消息已读
     * @param markReadDTO
     * @return
     */
    @PostMapping("/read")
    @ApiOperation("标记消息已读")
    public Result markMessagesAsRead(@RequestBody MarkReadDTO markReadDTO) {
        log.info("标记消息已读：{}", markReadDTO);
        chatService.markMessagesAsRead(markReadDTO.getFromUserId());
        return Result.success("已标记已读");
    }
}
