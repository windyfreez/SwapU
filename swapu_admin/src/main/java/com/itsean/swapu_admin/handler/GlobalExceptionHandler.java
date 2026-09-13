package com.itsean.swapu_admin.handler;

import com.itsean.pojo.Result;
import com.itsean.swapu_admin.exception.AccountRepeatException;
import com.itsean.swapu_admin.exception.AdminException;
import com.itsean.swapu_admin.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 管理员模块业务异常，直接携带模块错误码返回
     */
    @ExceptionHandler(AdminException.class)
    public Result handleAdminException(AdminException ex) {
        log.warn("管理员模块业务异常: code={}, msg={}", ex.getCode(), ex.getMessage());
        return error(ex);
    }

    @ExceptionHandler(AccountRepeatException.class)
    public Result handleAccountRepeatException(AccountRepeatException ex) {
        log.warn("账号重复异常: code={}, msg={}", ex.getCode(), ex.getMessage());
        return error(ex);
    }

    @ExceptionHandler(BaseException.class)
    public Result handleBaseException(BaseException ex) {
        log.warn("业务异常: code={}, msg={}", ex.getCode(), ex.getMessage());
        return error(ex);
    }

    /**
     * 兜底异常，禁止向客户端泄露堆栈信息
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception ex) {
        log.error("系统异常: ", ex);
        return Result.error("系统繁忙，请稍后重试");
    }

    /**
     * 按业务异常自身携带的错误码封装统一返回结果
     *
     * @param ex 业务异常
     * @return 携带模块错误码的失败结果
     */
    private Result error(BaseException ex) {
        Result result = Result.error(ex.getMessage());
        result.setCode(ex.getCode());
        return result;
    }
}
