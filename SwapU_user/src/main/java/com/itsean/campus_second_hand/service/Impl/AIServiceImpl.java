package com.itsean.campus_second_hand.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itsean.common.constant.MessageConstant;
import com.itsean.common.constant.StringConstant;
import com.itsean.common.context.BaseContext;
import com.itsean.campus_second_hand.dto.AiChatDTO;
import com.itsean.campus_second_hand.dto.AiMessageDTO;
import com.itsean.campus_second_hand.exception.AIException;
import com.itsean.campus_second_hand.properties.DeepSeekProperties;
import com.itsean.campus_second_hand.service.AIService;
import com.itsean.campus_second_hand.vo.AiChatVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * AI 客服服务实现：调用 DeepSeek 的 OpenAI 兼容接口
 * <p>
 * 普通问答用 hutool 一次性取回；流式问答必须用 JDK 的 HttpClient，
 * 因为 hutool 的同步请求会先把整个响应体读完才返回，拿不到逐块到达的增量。
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

    /**
     * SSE 事件名：增量正文
     */
    private static final String EVENT_DELTA = "delta";

    /**
     * SSE 事件名：正常结束
     */
    private static final String EVENT_DONE = "done";

    /**
     * SSE 事件名：异常
     */
    private static final String EVENT_ERROR = "error";

    /**
     * SSE 数据行前缀 "data:" 的长度
     */
    private static final int DATA_PREFIX_LENGTH = 5;

    /**
     * 上游流式结束标记
     */
    private static final String DATA_DONE = "[DONE]";

    /**
     * 容器超时相对上游读超时的富余量（毫秒）
     */
    private static final long STREAM_TIMEOUT_EXTRA_MILLIS = 5000L;

    @Autowired
    private DeepSeekProperties deepSeekProperties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 流式回答专用线程池，与 Tomcat 请求线程隔离
     */
    @Resource(name = "aiStreamExecutor")
    private ThreadPoolTaskExecutor aiStreamExecutor;

    /**
     * 流式调用用的 HTTP 客户端，连接超时取自配置
     */
    private HttpClient httpClient;

    /**
     * 初始化 HTTP 客户端：属性注入完成后才能读到超时配置
     */
    @PostConstruct
    public void initHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(deepSeekProperties.getConnectTimeout()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * 向大模型发起一次客服问答
     * @param aiChatDTO 提问参数
     * @return AI 回答
     */
    @Override
    public AiChatVO chat(AiChatDTO aiChatDTO) {
        String prompt = validatePrompt(aiChatDTO);

        //每次调用都是付费的，先做频率控制再发请求
        checkRateLimit();

        String requestBody = buildRequestBody(aiChatDTO, prompt, false);
        long startTime = System.currentTimeMillis();
        JSONObject responseJson = doRequest(requestBody);
        long costMillis = System.currentTimeMillis() - startTime;

        return parseResponse(responseJson, costMillis);
    }

    /**
     * 流式问答：校验与限流同步完成，模型调用交给线程池边收边推
     * @param aiChatDTO 提问参数
     * @return SSE 事件流
     */
    @Override
    public SseEmitter chatStream(AiChatDTO aiChatDTO) {
        String prompt = validatePrompt(aiChatDTO);
        checkRateLimit();

        String requestBody = buildRequestBody(aiChatDTO, prompt, true);
        //容器超时比上游读超时略长，避免上游还在生成就被容器掐断
        long timeoutMillis = deepSeekProperties.getReadTimeout() + STREAM_TIMEOUT_EXTRA_MILLIS;
        SseEmitter emitter = new SseEmitter(timeoutMillis);
        StreamState state = new StreamState();

        //客户端断开、超时或出错时必须关掉上游连接，否则线程会一直挂在读流上
        emitter.onCompletion(state::closeQuietly);
        emitter.onTimeout(() -> {
            state.closeQuietly();
            completeQuietly(emitter);
        });
        emitter.onError(throwable -> {
            state.closeQuietly();
            log.warn("AI 流式连接异常：{}", throwable.getMessage());
        });

        aiStreamExecutor.execute(() -> streamFromModel(requestBody, emitter, state));
        return emitter;
    }

    /**
     * 校验提问内容
     * @param aiChatDTO 提问参数
     * @return 去掉首尾空格后的提问
     */
    private String validatePrompt(AiChatDTO aiChatDTO) {
        if (aiChatDTO == null || StrUtil.isBlank(aiChatDTO.getPrompt())) {
            throw new AIException(MessageConstant.AI_PROMPT_EMPTY);
        }

        String prompt = aiChatDTO.getPrompt().trim();
        if (prompt.length() > MAX_PROMPT_LENGTH) {
            throw new AIException(MessageConstant.AI_PROMPT_TOO_LONG);
        }
        return prompt;
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
     * <p>
     * 包级可见是为了让单元测试能直接校验 stream 开关与 stream_options，
     * 业务代码只从 {@link #chat} 与 {@link #chatStream} 两处调用。
     * @param aiChatDTO 提问参数
     * @param prompt 已校验并去空格的提问
     * @param stream 是否流式返回
     * @return 请求体 JSON 字符串
     */
    String buildRequestBody(AiChatDTO aiChatDTO, String prompt, boolean stream) {
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
                .set("stream", stream)
                .set("max_tokens", deepSeekProperties.getMaxTokens());

        if (stream) {
            //让最后一帧带上 usage，否则流式模式下拿不到 token 用量
            body.set("stream_options", new JSONObject().set("include_usage", true));
        }

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

            if (status != 200) {
                throw mapErrorStatus(status, body);
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
     * 把上游 HTTP 状态码翻译成用户能看懂的提示
     * @param status HTTP 状态码
     * @param body 响应体，仅用于日志
     * @return 对应的业务异常
     */
    private AIException mapErrorStatus(int status, String body) {
        //以下错误码重试无用，直接按语义转成用户能看懂的提示
        if (status == 401 || status == 403) {
            log.error("DeepSeek 密钥无效或无权访问，status: {}", status);
            return new AIException(MessageConstant.AI_NOT_CONFIGURED);
        }
        if (status == 402) {
            log.error("DeepSeek 账户余额不足，status: {}", status);
            return new AIException(MessageConstant.AI_BALANCE_NOT_ENOUGH);
        }
        if (status == 429) {
            log.warn("DeepSeek 触发上游限流，status: {}", status);
            return new AIException(MessageConstant.AI_REQUEST_TOO_FREQUENT);
        }
        log.error("DeepSeek 接口返回异常，status: {}，body: {}", status, body);
        return new AIException(MessageConstant.AI_SERVICE_UNAVAILABLE);
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

    /**
     * 读取上游流式响应并逐条推给前端
     * @param requestBody 请求体 JSON 字符串
     * @param emitter SSE 发射器
     * @param state 流状态，用于客户端断开时关闭上游
     */
    private void streamFromModel(String requestBody, SseEmitter emitter, StreamState state) {
        String apiKey = deepSeekProperties.getApiKey();
        if (StrUtil.isBlank(apiKey)) {
            log.error("未配置 DeepSeek api-key，请在环境变量 DEEPSEEK_API_KEY 中设置");
            sendError(emitter, MessageConstant.AI_NOT_CONFIGURED);
            completeQuietly(emitter);
            return;
        }

        String url = deepSeekProperties.getBaseUrl() + CHAT_COMPLETION_PATH;
        long startTime = System.currentTimeMillis();
        String model = null;
        String finishReason = null;
        Integer totalTokens = null;
        StringBuilder answer = new StringBuilder();

        try {
            //hutool 与 JDK 都有 HttpRequest/HttpResponse，这里显式用全限定名取 JDK 版本
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(
                            deepSeekProperties.getReadTimeout() + STREAM_TIMEOUT_EXTRA_MILLIS))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(
                            requestBody, StandardCharsets.UTF_8))
                    .build();

            java.net.http.HttpResponse<Stream<String>> response =
                    httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofLines());

            if (response.statusCode() != 200) {
                //错误响应体不大，取前若干行用于日志即可
                String detail = response.body().limit(20).collect(Collectors.joining("\n"));
                AIException exception = mapErrorStatus(response.statusCode(), detail);
                sendError(emitter, exception.getMessage());
                return;
            }

            state.setUpstream(response.body());
            Iterator<String> iterator = response.body().iterator();

            while (!state.isClosed() && iterator.hasNext()) {
                String line = iterator.next();
                //SSE 只有 data 行带正文，注释行与空行直接跳过
                if (!line.startsWith("data:")) {
                    continue;
                }

                String payload = line.substring(DATA_PREFIX_LENGTH).trim();
                if (DATA_DONE.equals(payload)) {
                    break;
                }

                JSONObject chunk = parseChunk(payload);
                if (chunk == null) {
                    continue;
                }

                if (StrUtil.isNotBlank(chunk.getStr("model"))) {
                    model = chunk.getStr("model");
                }
                JSONObject usage = chunk.getJSONObject("usage");
                if (usage != null) {
                    totalTokens = usage.getInt("total_tokens");
                }

                JSONArray choices = chunk.getJSONArray("choices");
                if (choices == null || choices.isEmpty()) {
                    continue;
                }

                JSONObject choice = choices.getJSONObject(0);
                if (StrUtil.isNotBlank(choice.getStr("finish_reason"))) {
                    finishReason = choice.getStr("finish_reason");
                }

                JSONObject delta = choice.getJSONObject("delta");
                String content = delta == null ? null : delta.getStr("content");
                if (StrUtil.isNotEmpty(content)) {
                    answer.append(content);
                    sendEvent(emitter, EVENT_DELTA, new JSONObject().set("content", content).toString());
                }
            }

            long costMillis = System.currentTimeMillis() - startTime;
            if (answer.length() == 0) {
                log.error("AI 流式回答为空，finishReason: {}", finishReason);
                sendError(emitter, MessageConstant.AI_EMPTY_ANSWER);
                return;
            }

            JSONObject done = new JSONObject()
                    .set("model", model)
                    .set("totalTokens", totalTokens)
                    .set("finishReason", finishReason)
                    .set("costMillis", costMillis);
            sendEvent(emitter, EVENT_DONE, done.toString());

            //只记录用量与耗时，不打完整回答
            log.info("AI客服流式调用完成，模型: {}，耗时: {}ms，tokens: {}，字符数: {}",
                    model, costMillis, totalTokens, answer.length());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("AI 流式任务被中断");
            sendError(emitter, MessageConstant.AI_SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            if (state.isClosed()) {
                //客户端已经断开属于正常终止，不用再往已关闭的连接里写
                log.debug("AI 流式连接已被客户端关闭：{}", e.getMessage());
                return;
            }
            log.error("AI 流式调用失败，url: {}，原因: {}", url, e.getMessage());
            sendError(emitter, MessageConstant.AI_SERVICE_UNAVAILABLE);
        } finally {
            state.closeQuietly();
            completeQuietly(emitter);
        }
    }

    /**
     * 解析单帧增量数据，格式异常时跳过而不是中断整段回答
     * @param payload data 行内容
     * @return 解析结果，失败返回 null
     */
    private JSONObject parseChunk(String payload) {
        try {
            return JSONUtil.parseObj(payload);
        } catch (Exception e) {
            log.warn("忽略无法解析的流式数据：{}", StrUtil.maxLength(payload, 120));
            return null;
        }
    }

    /**
     * 发送一个 SSE 事件
     * @param emitter SSE 发射器
     * @param event 事件名
     * @param data 事件数据（JSON 字符串，内部已转义换行）
     * @throws IOException 连接已断开时抛出
     */
    private void sendEvent(SseEmitter emitter, String event, String data) throws IOException {
        emitter.send(SseEmitter.event().name(event).data(data));
    }

    /**
     * 发送错误事件，连接已断开时静默忽略
     * @param emitter SSE 发射器
     * @param message 错误提示
     */
    private void sendError(SseEmitter emitter, String message) {
        try {
            sendEvent(emitter, EVENT_ERROR, new JSONObject().set("msg", message).toString());
        } catch (Exception e) {
            log.debug("错误事件发送失败（连接可能已断开）：{}", e.getMessage());
        }
    }

    /**
     * 结束 SSE 连接，重复调用或连接已关闭时忽略异常
     * @param emitter SSE 发射器
     */
    private void completeQuietly(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("结束 SSE 连接失败（可能已完成）：{}", e.getMessage());
        }
    }

    /**
     * 单次流式调用的状态，用于在客户端断开时关闭上游连接
     */
    private static class StreamState {

        /**
         * 是否已关闭，多线程可见
         */
        private volatile boolean closed = false;

        /**
         * 上游响应体流，可能尚未建立
         */
        private volatile Stream<String> upstream;

        void setUpstream(Stream<String> upstream) {
            this.upstream = upstream;
            //赋值期间客户端可能已经断开，补一次关闭
            if (closed) {
                closeQuietly();
            }
        }

        boolean isClosed() {
            return closed;
        }

        void closeQuietly() {
            closed = true;
            Stream<String> stream = this.upstream;
            this.upstream = null;
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception ignore) {
                    //关闭失败无需处理，连接最终会被回收
                }
            }
        }
    }
}
