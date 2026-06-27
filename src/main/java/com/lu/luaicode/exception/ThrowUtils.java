package com.lu.luaicode.exception;

/**
 * 异常抛出工具类
 *
 * <p>提供便捷的条件断言式异常抛出，减少 if-throw 样板代码。</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * ThrowUtil.throwIf(id == null, "ID不能为空");
 * ThrowUtil.throwIf(user == null, ResultCode.USER_NOT_EXIST);
 * ThrowUtil.throwIfNot(hasPermission, ResultCode.FORBIDDEN);
 * ThrowUtil.badRequest("参数不合法");
 * ThrowUtil.unauthorized("登录已过期");
 * }</pre>
 */
public final class ThrowUtils {

    private ThrowUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // ==================== 条件抛出 ====================

    /**
     * 条件为 true 时抛出业务异常
     *
     * @param condition 触发条件
     * @param message   异常描述
     */
    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BusinessException(message);
        }
    }

    /**
     * 条件为 true 时抛出业务异常
     *
     * @param condition  触发条件
     * @param resultCode 预定义错误码
     */
    public static void throwIf(boolean condition, ResultCode resultCode) {
        if (condition) {
            throw new BusinessException(resultCode);
        }
    }

    /**
     * 条件为 true 时抛出业务异常
     *
     * @param condition  触发条件
     * @param resultCode 预定义错误码
     * @param message    自定义描述（覆盖枚举默认描述）
     */
    public static void throwIf(boolean condition, ResultCode resultCode, String message) {
        if (condition) {
            throw new BusinessException(resultCode, message);
        }
    }

    /**
     * 条件为 true 时抛出业务异常
     *
     * @param condition 触发条件
     * @param code      自定义错误码
     * @param message   异常描述
     */
    public static void throwIf(boolean condition, int code, String message) {
        if (condition) {
            throw new BusinessException(code, message);
        }
    }

    /**
     * 条件为 false 时抛出业务异常
     *
     * @param condition 触发条件
     * @param message   异常描述
     */
    public static void throwIfNot(boolean condition, String message) {
        if (!condition) {
            throw new BusinessException(message);
        }
    }

    /**
     * 条件为 false 时抛出业务异常
     *
     * @param condition  触发条件
     * @param resultCode 预定义错误码
     */
    public static void throwIfNot(boolean condition, ResultCode resultCode) {
        if (!condition) {
            throw new BusinessException(resultCode);
        }
    }

    // ==================== 快捷抛出 ====================

    /**
     * 直接抛出业务异常
     *
     * @param message 异常描述
     */
    public static void fail(String message) {
        throw new BusinessException(message);
    }

    /**
     * 直接抛出业务异常
     *
     * @param resultCode 预定义错误码
     */
    public static void fail(ResultCode resultCode) {
        throw new BusinessException(resultCode);
    }

    /**
     * 直接抛出业务异常
     *
     * @param code    自定义错误码
     * @param message 异常描述
     */
    public static void fail(int code, String message) {
        throw new BusinessException(code, message);
    }

    // ==================== 常用场景快捷方法 ====================

    /**
     * 抛出请求参数错误异常
     *
     * @param message 错误描述
     */
    public static void badRequest(String message) {
        throw new BusinessException(ResultCode.BAD_REQUEST, message);
    }

    /**
     * 抛出未授权异常
     *
     * @param message 错误描述
     */
    public static void unauthorized(String message) {
        throw new BusinessException(ResultCode.PARAM_TYPE_ERROR, message);
    }

    /**
     * 抛出无权限异常
     *
     * @param message 错误描述
     */
    public static void forbidden(String message) {
        throw new BusinessException(ResultCode.FORBIDDEN, message);
    }

    /**
     * 抛出资源不存在异常
     *
     * @param message 错误描述
     */
    public static void notFound(String message) {
        throw new BusinessException(ResultCode.NOT_FOUND, message);
    }

    /**
     * 抛出数据已存在异常
     *
     * @param message 错误描述
     */
    public static void alreadyExist(String message) {
        throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, message);
    }

    /**
     * 抛出数据不存在异常
     *
     * @param message 错误描述
     */
    public static void notExist(String message) {
        throw new BusinessException(ResultCode.DATA_NOT_EXIST, message);
    }
}
