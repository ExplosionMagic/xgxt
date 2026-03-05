package com.zjy.xgxt.common.exception;

import com.zjy.xgxt.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获我们在 Service 中抛出的业务异常 (如密码错误、未登录等)
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        e.printStackTrace(); // 在控制台打印错误，方便后端调试
        return Result.error(e.getMessage());
    }

    // 捕获其他未知系统异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error("系统内部错误，请联系管理员");
    }
}