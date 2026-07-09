package com.lu.luaicode.mq;

import com.lu.luaicode.service.ScreenshotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
public class ScreenshotTaskConsumer {

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private ScreenshotTaskProducer taskProducer;

    @RabbitListener(queues = "screenshot.task.queue", containerFactory = "screenshotListenerFactory")
    public void handleTask(ScreenshotTaskMessage message) {
        log.info("开始处理截图任务: requestId={}, appId={}, url={}", message.getRequestId(), message.getAppId(), message.getUrl());

        String cosUrl = null;
        boolean success = false;
        String errorMessage = null;

        try {
            cosUrl = screenshotService.generateAndUploadScreenshot(message.getUrl());
            if (cosUrl != null) {
                success = true;
                log.info("截图任务完成: requestId={}, cosUrl={}", message.getRequestId(), cosUrl);
            } else {
                errorMessage = "截图生成返回空结果";
                log.warn("截图任务返回空: requestId={}", message.getRequestId());
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.error("截图任务处理失败: requestId={}, appId={}", message.getRequestId(), message.getAppId(), e);
            throw e;
        } finally {
            taskProducer.sendResult(new ScreenshotResultMessage(
                    message.getRequestId(),
                    message.getAppId(),
                    message.getUrl(),
                    cosUrl,
                    success,
                    errorMessage
            ));
        }
    }
}
