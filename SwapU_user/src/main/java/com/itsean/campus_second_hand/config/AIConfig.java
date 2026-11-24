package com.itsean.campus_second_hand.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * AI 相关线程池配置
 * <p>
 * 流式回答会长时间占用线程（一次生成可能几十秒），必须与 Tomcat 请求线程隔离，
 * 否则并发一高就会把正常的商品/订单请求也拖住。
 */
@Configuration
public class AIConfig {

    /**
     * AI 流式回答专用线程池
     * <p>
     * 核心线程 4、最大 16、排队 64；队列满时回退到调用线程执行，形成背压而不是直接丢弃。
     * @return 线程池任务执行器
     */
    @Bean("aiStreamExecutor")
    public ThreadPoolTaskExecutor aiStreamExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(64);
        executor.setThreadNamePrefix("ai-stream-");
        // 关闭时不等待未完成的流式任务，避免应用停不下来
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
