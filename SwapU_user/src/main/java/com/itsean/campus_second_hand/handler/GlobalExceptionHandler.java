package com.itsean.campus_second_hand.handler;

import com.itsean.common.exception.AccountRepeatException;
import com.itsean.common.exception.BaseException;
import com.itsean.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 * <p>
 * 说明：业务异常已统一收敛到 com.itsean.common.exception.BaseException，
 * 这里只需保留一个 BaseException 处理器，避免对同一异常类型重复注册（Spring 启动会报 Ambiguous @ExceptionHandler）。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 账号重复类业务异常
     * @param ex 异常
     * @return 统一失败结果
     */
    @ExceptionHandler(AccountRepeatException.class)
    public Result handleAccountRepeatException(AccountRepeatException ex) {
        log.error("账号重复异常: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 业务异常基类，覆盖订单、评论、聊天、AI 等所有业务校验失败
     * @param ex 异常
     * @return 统一失败结果
     */
    @ExceptionHandler(BaseException.class)
    public Result handleBaseException(BaseException ex) {
        log.error("业务异常: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 兜底异常，禁止向客户端泄露堆栈信息
     * @param ex 异常
     * @return 统一失败结果
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception ex) {
        log.error("系统异常: ", ex);
        return Result.error("系统繁忙，请稍后重试");
    }
}
