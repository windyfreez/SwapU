package com.itsean.swapu_admin.exception;

import com.itsean.common.exception.BaseException;

/**
 * 分类模块业务异常
 */
public class CategoryException extends BaseException {

    public CategoryException(String message) {
        super(message);
    }

    public CategoryException(String message, Integer code) {
        super(message, code);
    }

}
