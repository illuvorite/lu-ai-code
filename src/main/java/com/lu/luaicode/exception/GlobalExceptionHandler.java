package com.lu.luaicode.exception;

import cn.hutool.core.util.StrUtil;
import com.lu.luaicode.common.Result;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 用于统一处理系统中抛出的各种异常，并返回统一的错误响应格式
 */
@Hidden
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    /**
     * 处理业务异常
     * @param e 业务异常对象
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    // ==================== 参数校验异常 ====================

    /**
     * 处理方法参数校验失败异常
     * @param e 方法参数校验异常对象
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> StrUtil.format("{}{}", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.fail(ResultCode.PARAM_VALUE_INVALID.getCode(), message);
    }

    /**
     * 处理参数绑定异常
     * @param e 参数绑定异常对象
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return Result.fail(ResultCode.PARAM_VALUE_INVALID, message);
    }

    /**
     * 处理约束校验异常
     * @param e 约束校验异常对象
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束校验失败: {}", message);
        return Result.fail(ResultCode.PARAM_VALUE_INVALID, message);
    }


    /**
     * 处理请求体解析失败异常
     * @param e 请求体解析失败异常对象
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, "请求体格式错误或为空");
    }

    // ==================== HTTP 异常 ====================

    /**
     * 处理不支持的请求方法异常
     * @param e 不支持的请求方法异常对象
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("不支持的请求方法: {}", e.getMethod());
        return Result.fail(ResultCode.METHOD_NOT_ALLOWED);
    }

    /**
     * 处理不支持的媒体类型异常
     * @param e 不支持的媒体类型异常对象
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<Void> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("不支持的媒体类型: {}", e.getContentType());
        return Result.fail(ResultCode.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * 处理资源不存在异常
     * @param e 资源不存在异常对象
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public Result<Void> handleNotFound(Exception e) {
        log.warn("资源不存在: {}", e.getMessage());
        return Result.fail(ResultCode.NOT_FOUND);
    }

    // ==================== 文件上传异常 ====================

    /**
     * 处理文件上传大小超过限制异常
     * @param e 文件上传大小超过限制异常对象
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.warn("文件上传超过大小限制: {}", e.getMessage());
        return Result.fail(ResultCode.FILE_UPLOAD_FAIL, "上传文件大小超过限制");
    }

    // ==================== 数据库异常 ====================

    /**
     * 处理主键重复异常
     * @param e 主键重复异常对象
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("数据重复: {}", e.getMessage());
        return Result.fail(ResultCode.DATA_ALREADY_EXIST);
    }

    // ==================== 兜底异常 ====================

    /**
     * 处理系统其他异常
     * @param e 系统异常对象
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }
}
