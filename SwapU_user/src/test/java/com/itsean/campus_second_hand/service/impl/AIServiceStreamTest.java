package com.itsean.campus_second_hand.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itsean.campus_second_hand.dto.AiChatDTO;
import com.itsean.campus_second_hand.dto.AiMessageDTO;
import com.itsean.campus_second_hand.exception.AIException;
import com.itsean.campus_second_hand.properties.DeepSeekProperties;
import com.itsean.common.constant.MessageConstant;
import com.itsean.common.context.BaseContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * AI 流式问答测试：入参守卫、限流、请求体 stream 开关
 * <p>
 * 不启动 Spring 容器、不连数据库、不真的访问大模型。
 */
@ExtendWith(MockitoExtension.class)
class AIServiceStreamTest {

    private static final Long USER_ID = 1001L;

    @Mock
    private DeepSeekProperties deepSeekProperties;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ThreadPoolTaskExecutor aiStreamExecutor;

    @InjectMocks
    private AIServiceImpl aiService;

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    private AiChatDTO dto(String prompt) {
        AiChatDTO aiChatDTO = new AiChatDTO();
        aiChatDTO.setPrompt(prompt);
        return aiChatDTO;
    }

    /** 流式路径需要读到的基础配置，只在真正走到建流时才会用到 */
    private void stubStreamProperties() {
        when(deepSeekProperties.getModel()).thenReturn("deepseek-flash");
        when(deepSeekProperties.getMaxTokens()).thenReturn(2048);
        when(deepSeekProperties.isThinking()).thenReturn(false);
        when(deepSeekProperties.getReadTimeout()).thenReturn(120000);
    }

    @Test
    @DisplayName("提问为空时直接拒绝，不会建流")
    void blankPromptRejected() {
        AIException ex = assertThrows(AIException.class, () -> aiService.chatStream(dto("   ")));

        assertEquals(MessageConstant.AI_PROMPT_EMPTY, ex.getMessage());
        verifyNoInteractions(aiStreamExecutor);
    }

    @Test
    @DisplayName("入参为 null 时按空提问处理")
    void nullDtoRejected() {
        AIException ex = assertThrows(AIException.class, () -> aiService.chatStream(null));

        assertEquals(MessageConstant.AI_PROMPT_EMPTY, ex.getMessage());
    }

    @Test
    @DisplayName("提问超过 2000 字时拒绝")
    void tooLongPromptRejected() {
        String prompt = "问".repeat(2001);

        AIException ex = assertThrows(AIException.class, () -> aiService.chatStream(dto(prompt)));

        assertEquals(MessageConstant.AI_PROMPT_TOO_LONG, ex.getMessage());
        verifyNoInteractions(aiStreamExecutor);
    }

    @Test
    @DisplayName("超过每分钟次数上限时拒绝，且不建流")
    void rateLimitedRejected() {
        BaseContext.setCurrentId(USER_ID);
        when(deepSeekProperties.getUserLimitPerMinute()).thenReturn(1);

        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = org.mockito.Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(2L);

        AIException ex = assertThrows(AIException.class, () -> aiService.chatStream(dto("怎么退货")));

        assertEquals(MessageConstant.AI_REQUEST_TOO_FREQUENT, ex.getMessage());
        verifyNoInteractions(aiStreamExecutor);
    }

    @Test
    @DisplayName("参数合法时返回 SSE 发射器，并把模型调用交给线程池")
    void validPromptReturnsEmitter() {
        BaseContext.setCurrentId(USER_ID);
        when(deepSeekProperties.getUserLimitPerMinute()).thenReturn(0);
        stubStreamProperties();

        SseEmitter emitter = aiService.chatStream(dto("怎么修改收货地址"));

        assertNotNull(emitter);
        verify(aiStreamExecutor, times(1)).execute(any(Runnable.class));
    }

    @Test
    @DisplayName("流式请求体必须带 stream=true 与 include_usage")
    void streamBodyEnablesStreaming() {
        when(deepSeekProperties.getModel()).thenReturn("deepseek-flash");
        when(deepSeekProperties.getMaxTokens()).thenReturn(2048);
        when(deepSeekProperties.isThinking()).thenReturn(false);

        JSONObject body = JSONUtil.parseObj(aiService.buildRequestBody(dto("你好"), "你好", true));

        assertTrue(body.getBool("stream"));
        assertTrue(body.getJSONObject("stream_options").getBool("include_usage"));
    }

    @Test
    @DisplayName("非流式请求体不带 stream_options")
    void nonStreamBodyHasNoStreamOptions() {
        when(deepSeekProperties.getModel()).thenReturn("deepseek-flash");
        when(deepSeekProperties.getMaxTokens()).thenReturn(2048);
        when(deepSeekProperties.isThinking()).thenReturn(false);

        JSONObject body = JSONUtil.parseObj(aiService.buildRequestBody(dto("你好"), "你好", false));

        assertFalse(body.getBool("stream"));
        assertFalse(body.containsKey("stream_options"));
    }

    @Test
    @DisplayName("历史消息最多带 10 条，超出部分从最早的开始丢弃")
    void historyTrimmedToLimit() {
        when(deepSeekProperties.getModel()).thenReturn("deepseek-flash");
        when(deepSeekProperties.getMaxTokens()).thenReturn(2048);
        when(deepSeekProperties.isThinking()).thenReturn(false);

        List<AiMessageDTO> history = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            history.add(new AiMessageDTO("user", "历史" + i));
        }
        AiChatDTO aiChatDTO = dto("最新提问");
        aiChatDTO.setHistory(history);

        JSONObject body = JSONUtil.parseObj(aiService.buildRequestBody(aiChatDTO, "最新提问", true));
        //1 条 system + 10 条历史 + 1 条本次提问
        assertEquals(12, body.getJSONArray("messages").size());

        //最早的 5 条应该已经被丢掉，保留的是 历史5 到 历史14
        assertEquals("历史5", body.getJSONArray("messages").getJSONObject(1).getStr("content"));
        assertEquals("最新提问", body.getJSONArray("messages").getJSONObject(11).getStr("content"));
    }
}
