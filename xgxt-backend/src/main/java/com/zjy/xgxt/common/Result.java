package com.zjy.xgxt.common;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result<T> {
    private Integer code; // 200成功, 401未登录, 500错误
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) { return new Result<>(200, "success", data); }
    public static <T> Result<T> error(String msg) { return new Result<>(500, msg, null); }
}
