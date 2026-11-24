package com.itsean.common.captcha;

import lombok.Data;

/**
 * 图形验证码下发给前端的数据
 * <p>
 * 图片直接以 base64 内联返回：前端只发一次请求就能拿到 key 与图片，
 * 不依赖 cookie，也不需要再为图片单独做一次带 key 的请求。
 */
@Data
public class CaptchaVO {

    /**
     * 本次验证码的标识，登录/注册时需要原样回传
     */
    private String captchaKey;

    /**
     * 图片内容，形如 data:image/png;base64,xxxx
     */
    private String captchaImage;

    /**
     * 有效期（秒），前端可用于展示倒计时
     */
    private Integer expireSeconds;
}
