package com.lu.luaicode.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ScreenshotTaskMessage 类是一个用于截图任务消息的数据传输对象(DTO)
 * 实现了Serializable接口，支持序列化操作
 * 使用了Lombok注解简化了getter、setter、构造函数等的编写
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenshotTaskMessage implements Serializable {

    /**
     * 序列化版本号，用于控制版本序列化/反序列化的兼容性
     */
    private static final long serialVersionUID = 1L;

    /**
     * 请求ID，用于唯一标识一个截图请求
     */
    private String requestId;
    /**
     * 应用ID，标识发起截图请求的应用
     */
    private Long appId;
    /**
     * 需要截图的网页URL地址
     */
    private String url;
}
