package com.itsean.common.exception;

/**
 * 图形验证码相关业务异常
 * <p>
 * 校验不通过属于用户可预期的错误，走 {@link BaseException}，
 * 由各应用的全局异常处理器统一转成 Result 返回。
 */
public class CaptchaException extends BaseException {

    public CaptchaException(String msg) {
        super(msg);
    }
}
