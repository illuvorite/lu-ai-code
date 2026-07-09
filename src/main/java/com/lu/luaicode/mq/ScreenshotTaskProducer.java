package com.lu.luaicode.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.UUID;

/**
 * 消息生产者
 */
@Slf4j
@Component
public class ScreenshotTaskProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送截图任务到消息队列
     */
    public void sendTask(Long appId, String url) {
        String requestId = UUID.randomUUID().toString();
        ScreenshotTaskMessage message = new ScreenshotTaskMessage(requestId, appId, url);//截图请求消息体
        rabbitTemplate.convertAndSend("screenshot.direct", "screenshot.task", message);
        log.info("截图任务已投递: requestId={}, appId={}, url={}", requestId, appId, url);
    }

    /**
     * 发送截图结果到结果队列
     */
    public void sendResult(ScreenshotResultMessage result) {
        rabbitTemplate.convertAndSend("screenshot.direct", "screenshot.result", result);
        log.debug("截图结果已投递: requestId={}, appId={}, success={}", result.getRequestId(), result.getAppId(), result.isSuccess());
    }
}
