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
}
