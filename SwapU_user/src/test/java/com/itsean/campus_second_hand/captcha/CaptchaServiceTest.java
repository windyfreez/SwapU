package com.itsean.campus_second_hand.captcha;

import com.itsean.common.captcha.CaptchaScene;
import com.itsean.common.captcha.CaptchaService;
import com.itsean.common.captcha.CaptchaVO;
import com.itsean.common.constant.MessageConstant;
import com.itsean.common.exception.CaptchaException;
import com.itsean.common.properties.CaptchaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 图形验证码服务测试：生成落库、一次性校验、过期、大小写、开关与限流
 * <p>
 * 不连 Redis：用 Mock 的 StringRedisTemplate 模拟读写。
 */
@ExtendWith(MockitoExtension.class)
class CaptchaServiceTest {

    private static final String CAPTCHA_KEY = "key-0001";

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private CaptchaProperties captchaProperties;

    private CaptchaService captchaService;

    @BeforeEach
    void setUp() {
        captchaProperties = new CaptchaProperties();
        captchaService = new CaptchaService(stringRedisTemplate, captchaProperties);
    }

    private String redisKey() {
        return "captcha:user-login:" + CAPTCHA_KEY;
    }

    private void stubRedisReadWrite() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("生成验证码：写入 Redis 并带回标识、base64 图片与有效期")
    void generateStoresAnswerInRedis() {
        stubRedisReadWrite();

        CaptchaVO captchaVO = captchaService.generate(CaptchaScene.USER_LOGIN, null);

        assertNotNull(captchaVO.getCaptchaKey());
        assertTrue(captchaVO.getCaptchaImage().startsWith("data:image/png;base64,"));
        assertEquals(captchaProperties.getExpireSeconds(), captchaVO.getExpireSeconds());
        // 键必须带场景前缀，避免不同入口的验证码互相覆盖
        verify(valueOperations).set(
                eq("captcha:user-login:" + captchaVO.getCaptchaKey()),
                anyString(),
                eq(Duration.ofSeconds(captchaProperties.getExpireSeconds())));
    }

    @Test
    @DisplayName("校验通过后删除答案，保证同一个码只能用一次")
    void verifySuccessDeletesKey() {
        stubRedisReadWrite();
        when(valueOperations.get(redisKey())).thenReturn("A7K2");

        captchaService.verify(CaptchaScene.USER_LOGIN, CAPTCHA_KEY, "A7K2");

        verify(stringRedisTemplate).delete(redisKey());
    }

    @Test
    @DisplayName("大小写不敏感，且自动去掉首尾空格")
    void verifyIgnoresCaseAndTrim() {
        stubRedisReadWrite();
        when(valueOperations.get(redisKey())).thenReturn("A7K2");

        captchaService.verify(CaptchaScene.USER_LOGIN, CAPTCHA_KEY, "  a7k2 ");
    }

    @Test
    @DisplayName("输入错误时同样删除答案，防止同一个码被反复尝试")
    void verifyWrongCodeDeletesKey() {
        stubRedisReadWrite();
        when(valueOperations.get(redisKey())).thenReturn("A7K2");

        CaptchaException ex = assertThrows(CaptchaException.class,
                () -> captchaService.verify(CaptchaScene.USER_LOGIN, CAPTCHA_KEY, "ZZZZ"));

        assertEquals(MessageConstant.CAPTCHA_INCORRECT, ex.getMessage());
        verify(stringRedisTemplate).delete(redisKey());
    }

    @Test
    @DisplayName("Redis 里没有答案时提示已过期")
    void verifyExpired() {
        stubRedisReadWrite();
        when(valueOperations.get(redisKey())).thenReturn(null);

        CaptchaException ex = assertThrows(CaptchaException.class,
                () -> captchaService.verify(CaptchaScene.USER_LOGIN, CAPTCHA_KEY, "A7K2"));

        assertEquals(MessageConstant.CAPTCHA_EXPIRED, ex.getMessage());
    }

    @Test
    @DisplayName("标识或字符为空时提示必填，且不访问 Redis")
    void verifyBlankArgs() {
        CaptchaException ex = assertThrows(CaptchaException.class,
                () -> captchaService.verify(CaptchaScene.USER_LOGIN, "", "A7K2"));

        assertEquals(MessageConstant.CAPTCHA_REQUIRED, ex.getMessage());
        verifyNoInteractions(stringRedisTemplate);
    }

    @Test
    @DisplayName("关闭开关后直接放行，方便本地调试")
    void verifySkippedWhenDisabled() {
        captchaProperties.setEnabled(false);

        captchaService.verify(CaptchaScene.USER_LOGIN, "whatever", "whatever");

        verifyNoInteractions(stringRedisTemplate);
    }

    @Test
    @DisplayName("同一 IP 每分钟超过上限时拒绝生成")
    void generateRateLimited() {
        captchaProperties.setIpLimitPerMinute(1);
        stubRedisReadWrite();
        when(valueOperations.increment(anyString())).thenReturn(2L);

        CaptchaException ex = assertThrows(CaptchaException.class,
                () -> captchaService.generate(CaptchaScene.USER_LOGIN, "127.0.0.1"));

        assertEquals(MessageConstant.CAPTCHA_REQUEST_TOO_FREQUENT, ex.getMessage());
        // 被限流时不应该再生成图片
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("限流首次计数时设置窗口过期时间")
    void generateRateLimitSetsExpire() {
        captchaProperties.setIpLimitPerMinute(30);
        stubRedisReadWrite();
        when(valueOperations.increment(anyString())).thenReturn(1L);
        doReturn(true).when(stringRedisTemplate).expire(anyString(), any(Duration.class));

        captchaService.generate(CaptchaScene.USER_LOGIN, "127.0.0.1");

        verify(stringRedisTemplate).expire(anyString(), eq(Duration.ofSeconds(60)));
    }
}
