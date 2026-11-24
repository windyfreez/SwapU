package com.itsean.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 图形验证码配置
 * <p>
 * 用户端与管理端各有一份 application.yml 配置，但读取的是同一个前缀。
 */
@Data
@ConfigurationProperties(prefix = "swapu.captcha")
public class CaptchaProperties {

    /**
     * 是否启用验证码校验；仅供本地调试或压测时关闭，生产环境必须为 true
     */
    private boolean enabled = true;

    /**
     * 验证码字符个数
     */
    private int length = 4;

    /**
     * 图片宽度（像素）
     */
    private int width = 130;

    /**
     * 图片高度（像素）
     */
    private int height = 48;

    /**
     * 干扰线数量
     */
    private int lineCount = 20;

    /**
     * 验证码有效期（秒）
     */
    private int expireSeconds = 120;

    /**
     * 同一 IP 每分钟最多生成次数，小于等于 0 表示不限流
     */
    private int ipLimitPerMinute = 30;
}
