package com.itsean.campus_second_hand.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 推荐系统大模型语义重排序相关配置
 */
@Component
@ConfigurationProperties(prefix = "swapu.recommend.rerank")
@Data
public class ReRankProperties {

    /**
     * 是否启用大模型语义重排序，关闭后推荐退化为纯召回分排序
     */
    private boolean enabled = true;

    /**
     * 参与重排序的最大候选商品数，超出部分按召回分截断，控制请求体大小与费用
     */
    private int maxCandidates = 30;

    /**
     * 单次重排序建立连接超时（毫秒），网络不通时快速失败
     */
    private int connectTimeout = 3000;

    /**
     * 单次重排序读取响应超时（毫秒），比客服问答短很多，避免拖慢推荐接口
     */
    private int readTimeout = 15000;
}
