package com.itsean.swapu_admin.exception;

/**
 * 管理员模块业务异常
 */
public class AdminException extends BaseException {

    public AdminException(String message) {
        super(message);
    }

    public AdminException(String message, Integer code) {
        super(message, code);
    }

}
