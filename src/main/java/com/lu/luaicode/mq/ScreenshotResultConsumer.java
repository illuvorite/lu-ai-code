package com.lu.luaicode.mq;

import com.lu.luaicode.model.dto.entity.App;
import com.lu.luaicode.service.AppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
public class ScreenshotResultConsumer {

    @Resource
    private AppService appService;

    @RabbitListener(queues = "screenshot.result.queue")
    public void handleResult(ScreenshotResultMessage message) {
        if (!message.isSuccess()) {
            log.warn("截图失败，不更新封面: requestId={}, appId={}, error={}",
                    message.getRequestId(), message.getAppId(), message.getErrorMessage());
            return;
        }

        try {
            App app = new App();
            app.setId(message.getAppId());
            app.setCover(message.getCosUrl());
            boolean updated = appService.updateById(app);
            if (updated) {
                log.info("应用封面已更新: appId={}, cosUrl={}", message.getAppId(), message.getCosUrl());
            } else {
                log.error("应用封面更新失败 (可能应用不存在): appId={}", message.getAppId());
            }
        } catch (Exception e) {
            log.error("应用封面更新异常: appId={}", message.getAppId(), e);
        }
    }
}
