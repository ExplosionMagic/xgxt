package com.zjy.xgxt.config;

import com.zjy.xgxt.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace(); // 打印堆栈信息方便调试
        return Result.error(e.getMessage() != null ? e.getMessage() : "系统内部错误");
    }
}
