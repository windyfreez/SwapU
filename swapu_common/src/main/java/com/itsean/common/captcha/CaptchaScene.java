package com.itsean.common.captcha;

import com.itsean.common.constant.MessageConstant;
import com.itsean.common.exception.CaptchaException;
import lombok.Getter;

/**
 * 图形验证码使用场景
 * <p>
 * 场景决定 Redis 键的命名空间，避免不同入口的验证码互相覆盖。
 */
@Getter
public enum CaptchaScene {

    /**
     * 用户端登录
     */
    USER_LOGIN("user-login", "用户登录"),

    /**
     * 用户端注册
     */
    USER_REGISTER("user-register", "用户注册"),

    /**
     * 管理端登录
     */
    ADMIN_LOGIN("admin-login", "管理员登录");

    /**
     * 对外传递的场景标识
     */
    private final String code;

    /**
     * 中文描述，仅用于日志
     */
    private final String desc;

    CaptchaScene(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据场景标识解析枚举
     * @param code 场景标识，忽略大小写
     * @return 对应场景
     */
    public static CaptchaScene of(String code) {
        for (CaptchaScene scene : values()) {
            if (scene.code.equalsIgnoreCase(code == null ? null : code.trim())) {
                return scene;
            }
        }
        throw new CaptchaException(MessageConstant.CAPTCHA_SCENE_INVALID);
    }
}
