package com.itsean.swapu_admin.exception;

import com.itsean.common.exception.BaseException;

/**
 * 审核模块业务异常（管理端）
 */
public class AuditException extends BaseException {

    public AuditException(String message) {
        super(message);
    }

    public AuditException(String message, Integer code) {
        super(message, code);
    }

}
