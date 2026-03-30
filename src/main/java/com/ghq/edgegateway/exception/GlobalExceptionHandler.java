package com.ghq.edgegateway.exception;

import com.ghq.edgegateway.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param exception 业务异常
     * @return 统一响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        return Result.failure(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理参数校验异常。
     *
     * @param exception 参数异常
     * @return 统一响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldError() == null
                ? "请求参数不合法"
                : exception.getBindingResult().getFieldError().getDefaultMessage();
        return Result.failure(400, message);
    }

    /**
     * 处理兜底异常。
     *
     * @param exception 未知异常
     * @return 统一响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        log.error("系统异常", exception);
        return Result.failure(500, "系统繁忙，请稍后重试");
    }
}
