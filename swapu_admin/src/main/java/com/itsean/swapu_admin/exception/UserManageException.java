package com.itsean.swapu_admin.exception;

/**
 * 用户管理模块业务异常（管理端）
 */
public class UserManageException extends BaseException {

    public UserManageException(String message) {
        super(message);
    }

    public UserManageException(String message, Integer code) {
        super(message, code);
    }

}
