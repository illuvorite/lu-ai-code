package com.lu.luaicode.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lu.luaicode.exception.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一API响应结果封装
 *
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // 忽略null值字段
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码：0表示成功，非0表示失败 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 请求追踪ID（可选，用于分布式链路追踪） */
    private String traceId;

    /** 响应时间戳 */
    private Long timestamp;

    // ==================== 成功响应 ====================

    /**
     * 成功响应（只带响应码和message）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        return result;
    }


    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 成功响应（带数据和自定义消息）
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = success(data);
        result.setMessage(message);
        return result;
    }



    // ==================== 失败响应 ====================

    /**
     * 失败响应（使用枚举）
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 失败响应（使用枚举 + 自定义消息）
     */
    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return fail(resultCode.getCode(), message);
    }

    /**
     * 失败响应（自定义状态码和消息）
     */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 失败响应（仅自定义消息，使用默认失败码）
     */
    public static <T> Result<T> fail(String message) {
        return fail(ResultCode.FAILURE.getCode(), message);
    }

    // ==================== 链式调用 ====================

/**
 * 设置追踪ID的方法
 * @param traceId 追踪ID字符串，用于标识一次请求或操作的唯一标识
 * @return 返回当前Result对象，支持链式调用
 */
    public Result<T> withTraceId(String traceId) {
        // 设置当前对象的traceId属性
        this.traceId = traceId;
        // 返回当前对象实例，支持方法链调用
        return this;
    }

    // ==================== 便捷判断方法 ====================

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }

    /**
     * 判断是否失败
     */
    public boolean isFail() {
        return !isSuccess();
    }
}
