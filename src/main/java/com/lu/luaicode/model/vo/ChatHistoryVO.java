package com.lu.luaicode.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史视图对象
 */
@Data
public class ChatHistoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String message;

    private String messageType;

    private Long appId;

    private Long userId;

    private LocalDateTime createTime;
}
