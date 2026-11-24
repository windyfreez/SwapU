package com.itsean.campus_second_hand.controller.user;

import cn.hutool.core.util.StrUtil;
import com.itsean.campus_second_hand.dto.AiChatDTO;
import com.itsean.campus_second_hand.service.AIService;
import com.itsean.campus_second_hand.vo.AiChatVO;
import com.itsean.pojo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/user/ai")
@Slf4j
@Api(tags = "AI客服接口")
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * AI 客服问答
     * @param aiChatDTO 提问参数
     * @return AI 回答
     */
    @PostMapping("/chat")
    @ApiOperation("AI客服问答")
    public Result<AiChatVO> chat(@RequestBody AiChatDTO aiChatDTO) {
        //只截取前 50 个字符，避免用户输入的手机号、地址等隐私信息整段进日志
        log.info("AI客服收到提问：{}", StrUtil.maxLength(aiChatDTO.getPrompt(), 50));
        return Result.success(aiService.chat(aiChatDTO));
    }

    /**
     * AI 客服流式问答（SSE）
     * <p>
     * 返回 text/event-stream：delta 为增量正文，done 为结束，error 为失败。
     * 参数校验与限流不通过时不会进入流式返回，而是走全局异常处理返回普通 JSON。
     * @param aiChatDTO 提问参数
     * @return SSE 事件流
     */
    @PostMapping("/chat/stream")
    @ApiOperation("AI客服问答（流式）")
    public SseEmitter chatStream(@RequestBody AiChatDTO aiChatDTO) {
        log.info("AI客服收到流式提问：{}", StrUtil.maxLength(aiChatDTO.getPrompt(), 50));
        return aiService.chatStream(aiChatDTO);
    }
}
