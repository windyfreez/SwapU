package com.itsean.swapu_admin.exception;

import com.itsean.swapu_admin.constant.ErrorCodeConstant;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BaseException extends RuntimeException {

    /** 业务错误码，默认 401（业务失败），子类可指定所属模块的错误码 */
    private Integer code = ErrorCodeConstant.UNAUTHORIZED;

    public BaseException() {
    }

    public BaseException(String msg) {
        super(msg);
    }

    public BaseException(String msg, Integer code) {
        super(msg);
        this.code = code;
    }

}
