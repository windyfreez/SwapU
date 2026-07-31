package com.itsean.swapu_admin.handler;

import com.itsean.swapu_admin.entity.result.Result;
import com.itsean.swapu_admin.exception.AccountRepeatException;
import com.itsean.swapu_admin.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountRepeatException.class)
    public Result handleAccountRepeatException(AccountRepeatException ex) {
        log.error("账号重复异常: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(BaseException.class)
    public Result handleBaseException(BaseException ex) {
        log.error("业务异常: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception ex) {
        log.error("系统异常: {}", ex.getMessage());
        return Result.error("系统繁忙，请稍后重试");
    }
}
