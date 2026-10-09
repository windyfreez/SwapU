package com.itsean.campus_second_hand.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek 大模型调用相关配置
 */
@Component
@ConfigurationProperties(prefix = "swapu.deepseek")
@Data
public class DeepSeekProperties {

    /**
     * 接口地址，DeepSeek 兼容 OpenAI 协议
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * 接口密钥，优先通过环境变量 DEEPSEEK_API_KEY 注入，禁止写死在代码或配置文件里
     */
    private String apiKey;

    /**
     * 模型名，当前可用：deepseek-flash（快）/ deepseek-v4-pro（强）
     */
    private String model = "deepseek-flash";

    /**
     * 是否开启思考模式，关闭更快更省，需要推理过程时再打开
     */
    private boolean thinking = false;

    /**
     * 思考强度，仅 thinking=true 时生效：none / low / high / max
     */
    private String reasoningEffort = "high";

    /**
     * 单次回答允许生成的最大 token 数
     */
    private int maxTokens = 2048;

    /**
     * 建立连接超时（毫秒），网络不通时快速失败
     */
    private int connectTimeout = 5000;

    /**
     * 读取响应超时（毫秒），大模型首字响应就要数秒，必须给足
     */
    private int readTimeout = 120000;

    /**
     * 单个用户每分钟最多调用次数，小于等于 0 表示不限流
     */
    private int userLimitPerMinute = 20;
}
