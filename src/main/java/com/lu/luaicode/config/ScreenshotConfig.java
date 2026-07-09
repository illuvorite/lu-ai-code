package com.lu.luaicode.config;

import com.lu.luaicode.utils.WebScreenshotUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@Slf4j
@EnableScheduling // 开启定时任务
public class ScreenshotConfig {


    /**
     * 定时清理过期截图文件，每天凌晨 2 点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void clearScreenshot() { // 方法名：clearScreenshot，清理截图文件
        log.info("开始清理过期截图文件"); // 记录开始清理的日志信息
        try {
            WebScreenshotUtils.cleanTempFiles(); // 调用工具类方法清理临时文件
            log.info("定时清理临时截图文件完成"); // 记录清理完成的日志信息
        } catch (Exception e) { // 捕获可能发生的异常
            log.error("清理过期截图文件时发生异常", e); // 记录异常日志，包含异常堆栈信息
        }
    }
}
