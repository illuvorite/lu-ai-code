package com.lu.luaicode.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
@AllArgsConstructor
public class StreamMessage {

    /**
     * 消息类型
     */
    private String type;
}
