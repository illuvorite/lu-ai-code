package com.lu.luaicode.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 截图结果消息类
 * 用于封装截图操作的结果信息，实现了Serializable接口以支持序列化
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenshotResultMessage implements Serializable {

    private static final long serialVersionUID = 1L;  // 序列化版本UID

    private String requestId;      // 请求ID，用于标识唯一的截图请求
    private Long appId;            // 应用ID，标识发起截图请求的应用
    private String url;            // 原始截图URL
    private String cosUrl;         // 存储在COS上的截图URL
    private boolean success;       // 截图操作是否成功
    private String errorMessage;   // 错误信息，当截图失败时记录具体的错误原因
}
