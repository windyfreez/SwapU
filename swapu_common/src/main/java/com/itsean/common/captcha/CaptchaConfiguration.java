package com.itsean.common.captcha;

import com.itsean.common.properties.CaptchaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 图形验证码装配类
 * <p>
 * 用户端与管理端这两个应用都不扫描 com.itsean.common 包，
 * 所以在各自启动类上用 {@code @Import(CaptchaConfiguration.class)} 显式接入，
 * 既能共用一份实现，又不会把公共模块里的其他类误注册成 Bean。
 */
@Configuration
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaConfiguration {

    /**
     * 注册验证码服务
     * @param stringRedisTemplate Redis 操作模板，由 Spring Boot 自动配置提供
     * @param captchaProperties 验证码配置
     * @return 验证码服务
     */
    @Bean
    public CaptchaService captchaService(StringRedisTemplate stringRedisTemplate,
                                         CaptchaProperties captchaProperties) {
        return new CaptchaService(stringRedisTemplate, captchaProperties);
    }
}
