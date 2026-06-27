package com.lu.luaicode.common;

/**
 * 错误码接口
 * 该接口用于定义错误码的基本结构，包含错误码和错误信息两个方法
 */
public interface IErrorCode {

    /**
     * 获取错误码
     * @return 返回错误码的整数值
     */
    int getCode();

    /**
     * 获取错误信息
     * @return 返回错误信息的字符串描述
     */
    String getMessage();
}
