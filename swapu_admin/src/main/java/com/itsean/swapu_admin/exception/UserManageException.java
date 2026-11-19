package com.itsean.swapu_admin.exception;

import com.itsean.common.exception.BaseException;

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
