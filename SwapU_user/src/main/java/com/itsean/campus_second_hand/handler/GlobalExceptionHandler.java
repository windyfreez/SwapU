package com.itsean.campus_second_hand.handler;

import com.itsean.common.exception.AccountRepeatException;
import com.itsean.common.exception.BaseException;
import com.itsean.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Objects;

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
     * @RequestBody 参数校验失败（@Valid + @NotNull/@Min 等）
     * @param ex 异常
     * @return 首个校验失败的提示文案
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = firstFieldError(ex.getBindingResult().getFieldErrors());
        log.warn("请求体参数校验失败: {}", msg);
        return Result.error(msg);
    }

    /**
     * 查询参数/表单绑定校验失败
     * @param ex 异常
     * @return 首个校验失败的提示文案
     */
    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException ex) {
        String msg = firstFieldError(ex.getBindingResult().getFieldErrors());
        log.warn("请求参数校验失败: {}", msg);
        return Result.error(msg);
    }

    /**
     * 方法参数级校验失败（如 @PathVariable + @Validated）
     * @param ex 异常
     * @return 首个校验失败的提示文案
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolation(ConstraintViolationException ex) {
        String msg = "参数校验失败";
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            if (violation.getMessage() != null) {
                msg = violation.getMessage();
                break;
            }
        }
        log.warn("参数校验失败: {}", msg);
        return Result.error(msg);
    }

    /**
     * 请求体不是合法 JSON 或字段类型不匹配，避免抛出 500
     * @param ex 异常
     * @return 统一失败结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("请求体解析失败: {}", ex.getMessage());
        return Result.error("请求参数格式不正确");
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

    /**
     * 取第一个字段校验失败的提示文案
     * @param fieldErrors 字段错误列表
     * @return 提示文案
     */
    private String firstFieldError(java.util.List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("参数校验失败");
    }
}
