package com.lu.luaicode.exception;

import lombok.Getter;

/**
 * 业务异常基类
 *
 * <p>统一业务异常，用于在业务逻辑层抛出可识别的异常信息。
 * 配合 {@link GlobalExceptionHandler} 可统一捕获并返回标准化的错误响应。</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 使用默认业务错误码
 * throw new BusinessException("用户不存在");
 *
 * // 使用预定义错误码
 * throw new BusinessException(ResultCode.UNAUTHORIZED);
 *
 * // 自定义错误码
 * throw new BusinessException(1001, "自定义错误");
 * }</pre>
 *
 * @see ResultCode
 * @see GlobalExceptionHandler
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    /**
     * 使用默认业务错误码构造异常
     *
     * @param message 错误描述信息
     */
    public BusinessException(String message) {
        this(ResultCode.BUSINESS_ERROR.getCode(), message);
    }

    /**
     * 使用预定义错误码构造异常
     *
     * @param resultCode 预定义错误码枚举
     */
    public BusinessException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 使用预定义错误码 + 自定义描述构造异常
     * <p>覆盖枚举中的默认描述，提供更具体的错误信息</p>
     *
     * @param resultCode 预定义错误码枚举
     * @param message    自定义错误描述信息
     */
    public BusinessException(ResultCode resultCode, String message) {
        this(resultCode.getCode(), message);
    }

    /**
     * 使用自定义错误码和描述构造异常
     *
     * @param code    自定义错误码
     * @param message 错误描述信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用默认业务错误码 + 异常原因构造异常
     *
     * @param message 错误描述信息
     * @param cause   原始异常（保留异常堆栈链）
     */
    public BusinessException(String message, Throwable cause) {
        this(ResultCode.BUSINESS_ERROR.getCode(), message, cause);
    }

    /**
     * 使用自定义错误码 + 异常原因构造异常
     *
     * @param code    自定义错误码
     * @param message 错误描述信息
     * @param cause   原始异常（保留异常堆栈链）
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
