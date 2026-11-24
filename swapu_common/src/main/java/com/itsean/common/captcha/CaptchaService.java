package com.itsean.common.captcha;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.itsean.common.constant.MessageConstant;
import com.itsean.common.constant.StringConstant;
import com.itsean.common.exception.CaptchaException;
import com.itsean.common.properties.CaptchaProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * 图形验证码服务
 * <p>
 * 生成：绘制带干扰线的字符图片，答案写入 Redis（带有效期），key 返回给前端。
 * 校验：取出答案后立即删除，因此同一个验证码只能用一次，无论这次是否输入正确。
 */
@Slf4j
public class CaptchaService {

    /**
     * 生成频率限制的统计窗口（秒）
     */
    private static final long LIMIT_WINDOW_SECONDS = 60L;

    private final StringRedisTemplate stringRedisTemplate;

    private final CaptchaProperties captchaProperties;

    public CaptchaService(StringRedisTemplate stringRedisTemplate, CaptchaProperties captchaProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.captchaProperties = captchaProperties;
    }

    /**
     * 生成一张图形验证码
     * @param scene 使用场景，决定 Redis 键的命名空间
     * @param ip 请求方 IP，用于生成频率限制，可为空
     * @return 验证码标识与图片
     */
    public CaptchaVO generate(CaptchaScene scene, String ip) {
        checkGenerateLimit(ip);

        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(
                captchaProperties.getWidth(),
                captchaProperties.getHeight(),
                captchaProperties.getLength(),
                captchaProperties.getLineCount());
        String code = captcha.getCode();
        String captchaKey = IdUtil.fastSimpleUUID();

        try {
            stringRedisTemplate.opsForValue().set(buildKey(scene, captchaKey), code,
                    Duration.ofSeconds(captchaProperties.getExpireSeconds()));
        } catch (Exception e) {
            log.error("图形验证码写入 Redis 失败，scene：{}", scene.getCode(), e);
            throw new CaptchaException(MessageConstant.CAPTCHA_SERVICE_UNAVAILABLE);
        }

        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaKey(captchaKey);
        captchaVO.setCaptchaImage(captcha.getImageBase64Data());
        captchaVO.setExpireSeconds(captchaProperties.getExpireSeconds());
        return captchaVO;
    }

    /**
     * 校验验证码，不通过直接抛业务异常
     * <p>
     * 一次性：校验时先把答案从 Redis 删掉，因此同一个验证码重复提交必然失败，
     * 前端在校验失败后需要刷新图片。
     * @param scene 使用场景
     * @param captchaKey 生成时返回的标识
     * @param captchaCode 用户输入的字符
     */
    public void verify(CaptchaScene scene, String captchaKey, String captchaCode) {
        if (!captchaProperties.isEnabled()) {
            log.warn("图形验证码校验已被关闭（swapu.captcha.enabled=false），仅限本地调试使用");
            return;
        }

        if (StrUtil.isBlank(captchaKey) || StrUtil.isBlank(captchaCode)) {
            throw new CaptchaException(MessageConstant.CAPTCHA_REQUIRED);
        }

        String redisKey = buildKey(scene, captchaKey);
        String expected;
        try {
            expected = stringRedisTemplate.opsForValue().get(redisKey);
            stringRedisTemplate.delete(redisKey);
        } catch (Exception e) {
            log.error("图形验证码校验时读取 Redis 失败，scene：{}", scene.getCode(), e);
            throw new CaptchaException(MessageConstant.CAPTCHA_SERVICE_UNAVAILABLE);
        }

        if (expected == null) {
            throw new CaptchaException(MessageConstant.CAPTCHA_EXPIRED);
        }
        if (!expected.equalsIgnoreCase(captchaCode.trim())) {
            log.warn("图形验证码不匹配，scene：{}", scene.getCode());
            throw new CaptchaException(MessageConstant.CAPTCHA_INCORRECT);
        }
    }

    /**
     * 拼接 Redis 键
     * @param scene 使用场景
     * @param captchaKey 验证码标识
     * @return Redis 键
     */
    private String buildKey(CaptchaScene scene, String captchaKey) {
        return StringConstant.CAPTCHA_REDIS_PREFIX + scene.getCode() + ":" + captchaKey;
    }

    /**
     * 按 IP 限制生成频率，防止脚本把生成接口刷爆
     * <p>
     * Redis 异常时放行生成：此时验证也拿不到答案，用户会看到「验证码已过期」，
     * 不会造成越权，但能避免整个登录页因为限流组件本身而打不开。
     * @param ip 请求方 IP
     */
    private void checkGenerateLimit(String ip) {
        int limit = captchaProperties.getIpLimitPerMinute();
        if (limit <= 0 || StrUtil.isBlank(ip)) {
            return;
        }

        //按自然分钟分桶，过期时间一到计数自动清零
        String key = StringConstant.CAPTCHA_LIMIT_PREFIX + ip + ":"
                + (System.currentTimeMillis() / (LIMIT_WINDOW_SECONDS * 1000));
        try {
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(key, Duration.ofSeconds(LIMIT_WINDOW_SECONDS));
            }
            if (count != null && count > limit) {
                log.warn("IP {} 触发生成验证码限流，本分钟第 {} 次", ip, count);
                throw new CaptchaException(MessageConstant.CAPTCHA_REQUEST_TOO_FREQUENT);
            }
        } catch (CaptchaException e) {
            throw e;
        } catch (Exception e) {
            log.warn("验证码生成限流校验失败，本次放行：{}", e.getMessage());
        }
    }
}
