package com.itsean.campus_second_hand.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itsean.campus_second_hand.constant.MessageConstant;
import com.itsean.campus_second_hand.constant.StringConstant;
import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.dto.AiChatDTO;
import com.itsean.campus_second_hand.dto.AiMessageDTO;
import com.itsean.campus_second_hand.exception.AIException;
import com.itsean.campus_second_hand.properties.DeepSeekProperties;
import com.itsean.campus_second_hand.service.AIService;
import com.itsean.campus_second_hand.vo.AiChatVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;

/**
 * AI 客服服务实现：用 hutool 的 HttpRequest 直接调用 DeepSeek 的 OpenAI 兼容接口
 */
@Service
@Slf4j
public class AIServiceImpl implements AIService {

    /**
     * 默认客服人设，调用方不传 systemPrompt 时使用
     */
    private static final String DEFAULT_SYSTEM_PROMPT =
            "你是 SwapU 云市集（校园二手交易平台）的智能客服助手。"
                    + "请用简洁、友好的中文回答用户关于商品浏览、下单、支付、发货、收货、退货退款、账号与余额等问题。"
                    + "只回答与平台和二手交易相关的内容，遇到无关问题请礼貌地把话题引导回平台业务。"
                    + "不确定的信息不要编造，可以建议用户联系人工客服。回答尽量控制在 200 字以内。";

    /**
     * 单次提问的字符数上限，防止把整篇文章塞进来
     */
    private static final int MAX_PROMPT_LENGTH = 2000;

    /**
     * 最多携带的历史消息条数，避免上下文过长既费钱又容易超限
     */
    private static final int MAX_HISTORY_SIZE = 10;

    /**
     * 限流统计窗口（秒）
     */
    private static final long LIMIT_WINDOW_SECONDS = 60L;

    /**
     * DeepSeek 对话补全接口路径
     */
    private static final String CHAT_COMPLETION_PATH = "/chat/completions";

    @Autowired
    private DeepSeekProperties deepSeekProperties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 向大模型发起一次客服问答
     * @param aiChatDTO 提问参数
     * @return AI 回答
     */
    @Override
    public AiChatVO chat(AiChatDTO aiChatDTO) {
        if (aiChatDTO == null || StrUtil.isBlank(aiChatDTO.getPrompt())) {
            throw new AIException(MessageConstant.AI_PROMPT_EMPTY);
        }

        String prompt = aiChatDTO.getPrompt().trim();
        if (prompt.length() > MAX_PROMPT_LENGTH) {
            throw new AIException(MessageConstant.AI_PROMPT_TOO_LONG);
        }

        //每次调用都是付费的，先做频率控制再发请求
        checkRateLimit();

        String requestBody = buildRequestBody(aiChatDTO, prompt);
        long startTime = System.currentTimeMillis();
        JSONObject responseJson = doRequest(requestBody);
        long costMillis = System.currentTimeMillis() - startTime;

        return parseResponse(responseJson, costMillis);
    }

    /**
     * 每用户每分钟调用次数限制；Redis 故障时放行，避免限流组件本身把客服搞挂
     */
    private void checkRateLimit() {
        int limit = deepSeekProperties.getUserLimitPerMinute();
        if (limit <= 0) {
            return;
        }

        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return;
        }

        //按自然分钟分桶，过期时间一到计数自动清零
        String key = StringConstant.AI_LIMIT_PREFIX + userId + ":"
                + (System.currentTimeMillis() / (LIMIT_WINDOW_SECONDS * 1000));
        try {
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(key, Duration.ofSeconds(LIMIT_WINDOW_SECONDS));
            }
            if (count != null && count > limit) {
                log.warn("用户{}触发AI客服限流，本分钟第{}次调用", userId, count);
                throw new AIException(MessageConstant.AI_REQUEST_TOO_FREQUENT);
            }
        } catch (AIException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AI客服限流校验失败，本次放行：{}", e.getMessage());
        }
    }

    /**
     * 拼装 OpenAI 兼容格式的请求体
     * @param aiChatDTO 提问参数
     * @param prompt 已校验并去空格的提问
     * @return 请求体 JSON 字符串
     */
    private String buildRequestBody(AiChatDTO aiChatDTO, String prompt) {
        JSONArray messages = new JSONArray();

        //人设：调用方指定优先，否则用默认客服人设
        String systemPrompt = StrUtil.isBlank(aiChatDTO.getSystemPrompt())
                ? DEFAULT_SYSTEM_PROMPT
                : aiChatDTO.getSystemPrompt().trim();
        messages.add(new JSONObject().set("role", "system").set("content", systemPrompt));

        //只带最近若干条历史，超出部分从最早的开始丢弃
        List<AiMessageDTO> history = aiChatDTO.getHistory();
        if (history != null && !history.isEmpty()) {
            int fromIndex = Math.max(0, history.size() - MAX_HISTORY_SIZE);
            for (AiMessageDTO message : history.subList(fromIndex, history.size())) {
                if (message == null || StrUtil.isBlank(message.getContent())) {
                    continue;
                }
                messages.add(new JSONObject()
                        .set("role", StrUtil.isBlank(message.getRole()) ? "user" : message.getRole())
                        .set("content", message.getContent()));
            }
        }

        messages.add(new JSONObject().set("role", "user").set("content", prompt));

        JSONObject body = new JSONObject()
                .set("model", deepSeekProperties.getModel())
                .set("messages", messages)
                .set("stream", false)
                .set("max_tokens", deepSeekProperties.getMaxTokens());

        //思考模式开关；关闭时不传 reasoning_effort，否则该参数会强行把思考打开
        if (deepSeekProperties.isThinking()) {
            body.set("thinking", new JSONObject().set("type", "enabled"));
            body.set("reasoning_effort", deepSeekProperties.getReasoningEffort());
        } else {
            body.set("thinking", new JSONObject().set("type", "disabled"));
        }

        return body.toString();
    }

    /**
     * 发起 HTTP 请求并取回 JSON，网络异常与各类错误码在这里统一转成业务异常
     * @param requestBody 请求体 JSON 字符串
     * @return 响应 JSON
     */
    private JSONObject doRequest(String requestBody) {
        String apiKey = deepSeekProperties.getApiKey();
        if (StrUtil.isBlank(apiKey)) {
            log.error("未配置 DeepSeek api-key，请在环境变量 DEEPSEEK_API_KEY 中设置");
            throw new AIException(MessageConstant.AI_NOT_CONFIGURED);
        }

        String url = deepSeekProperties.getBaseUrl() + CHAT_COMPLETION_PATH;
        try (HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey.trim())
                //hutool 默认按 UTF-8 编码请求体，第二个参数是 Content-Type 而不是字符集
                .body(requestBody, "application/json")
                .setConnectionTimeout(deepSeekProperties.getConnectTimeout())
                //大模型首字响应就要数秒，读超时必须给足，hutool 默认是无限等待
                .setReadTimeout(deepSeekProperties.getReadTimeout())
                .execute()) {

            int status = response.getStatus();
            String body = response.body();

            //以下错误码重试无用，直接按语义转成用户能看懂的提示
            if (status == 401 || status == 403) {
                log.error("DeepSeek 密钥无效或无权访问，status: {}", status);
                throw new AIException(MessageConstant.AI_NOT_CONFIGURED);
            }
            if (status == 402) {
                log.error("DeepSeek 账户余额不足，status: {}", status);
                throw new AIException(MessageConstant.AI_BALANCE_NOT_ENOUGH);
            }
            if (status == 429) {
                log.warn("DeepSeek 触发上游限流，status: {}", status);
                throw new AIException(MessageConstant.AI_REQUEST_TOO_FREQUENT);
            }
            if (status != 200) {
                log.error("DeepSeek 接口返回异常，status: {}，body: {}", status, body);
                throw new AIException(MessageConstant.AI_SERVICE_UNAVAILABLE);
            }

            try {
                return JSONUtil.parseObj(body);
            } catch (Exception e) {
                log.error("DeepSeek 响应解析失败，body: {}", body);
                throw new AIException(MessageConstant.AI_SERVICE_UNAVAILABLE);
            }
        } catch (AIException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 DeepSeek 接口失败，url: {}，原因: {}", url, e.getMessage());
            throw new AIException(MessageConstant.AI_SERVICE_UNAVAILABLE);
        }
    }

    /**
     * 从响应中取出回答正文与用量信息
     * @param responseJson 响应 JSON
     * @param costMillis 本次调用耗时（毫秒）
     * @return AI 回答
     */
    private AiChatVO parseResponse(JSONObject responseJson, long costMillis) {
        JSONArray choices = responseJson.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            log.error("DeepSeek 未返回 choices，响应: {}", responseJson);
            throw new AIException(MessageConstant.AI_EMPTY_ANSWER);
        }

        JSONObject choice = choices.getJSONObject(0);
        JSONObject message = choice.getJSONObject("message");
        String content = message == null ? null : message.getStr("content");
        if (StrUtil.isBlank(content)) {
            log.error("DeepSeek 返回内容为空，finishReason: {}", choice.getStr("finish_reason"));
            throw new AIException(MessageConstant.AI_EMPTY_ANSWER);
        }

        AiChatVO aiChatVO = new AiChatVO();
        aiChatVO.setContent(content.trim());
        aiChatVO.setReasoningContent(message.getStr("reasoning_content"));
        aiChatVO.setModel(responseJson.getStr("model"));
        aiChatVO.setFinishReason(choice.getStr("finish_reason"));

        JSONObject usage = responseJson.getJSONObject("usage");
        if (usage != null) {
            aiChatVO.setTotalTokens(usage.getInt("total_tokens"));
        }

        //只记录用量与耗时，不打完整提问和回答，避免用户输入的隐私信息进日志
        log.info("AI客服调用完成，模型: {}，耗时: {}ms，tokens: {}，finishReason: {}",
                aiChatVO.getModel(), costMillis, aiChatVO.getTotalTokens(), aiChatVO.getFinishReason());
        return aiChatVO;
    }
}
