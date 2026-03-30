package com.ghq.edgegateway.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体。
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Integer code;

    private String message;

    private T data;

    /**
     * 构造成功响应。
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 构造失败响应。
     *
     * @param code 业务码
     * @param message 失败消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> Result<T> failure(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
