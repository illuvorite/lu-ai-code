package com.lu.luaicode.pool;

import com.lu.luaicode.utils.WebScreenshotUtils;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * 使用 @Slf4j 注解提供日志功能，使用 @Component 注解标记为 Spring 组件
 */
@Slf4j
@Component
public class WebDriverPool {

    // 使用 BlockingQueue 实现线程安全的 WebDriver 对象池
    private final BlockingQueue<WebDriver> pool = new LinkedBlockingQueue<>();
    // 使用 Semaphore 控制并发访问的 WebDriver 数量
    private final Semaphore semaphore;
    // 连接池的最大容量，目前未使用但保留以备将来扩展
    @SuppressWarnings("unused")
    private final int maxSize;

/**
 * WebDriverPool 构造函数，用于初始化 WebDriver 连接池
 * @param maxSize 连接池最大容量，通过配置文件设置，默认值为4
 * @param minIdle 连接池最小空闲连接数，通过配置文件设置，默认值为1
 */
    public WebDriverPool(
            @Value("${screenshot.pool.max-size:4}") int maxSize,  // 从配置文件中获取最大连接数，若未设置则默认为4
            @Value("${screenshot.pool.min-idle:1}") int minIdle) {  // 从配置文件中获取最小空闲连接数，若未设置则默认为1
        // 初始化最大连接数
        this.maxSize = maxSize;
        // 初始化信号量，用于控制并发访问数量，设置为公平模式
        this.semaphore = new Semaphore(maxSize, true);
        // 计算预热连接数，确保在[minIdle, maxSize]范围内，且不小于0
        int warmSize = Math.min(Math.max(minIdle, 0), maxSize);
        // 执行预热操作，创建指定数量的 WebDriver 实例
        for (int i = 0; i < warmSize; i++) {
            try {
                // 初始化 Chrome WebDriver，设置窗口大小为1600x900
                WebDriver driver = WebScreenshotUtils.initChromeDriver(1600, 900);
                // 将创建的 WebDriver 实例添加到连接池中
                pool.offer(driver);
                // 记录预热成功的日志
                log.info("预热 WebDriver #{} 完成", i + 1);
            } catch (Exception e) {
                // 记录预热失败的日志
                log.error("预热 WebDriver #{} 失败", i + 1, e);
            }
        }
        // 记录连接池初始化完成的日志，包含最大连接数、最小空闲连接数和实际预热数量
        log.info("WebDriverPool 初始化完成, maxSize={}, minIdle={}, 实际预热={}", maxSize, minIdle, pool.size());
    }

    /**
     * 从池中借用 WebDriver，阻塞直到可用或超时
     * @return 可用的 WebDriver 实例
     */
    public WebDriver borrow() {
        try {
            // 获取信号量许可，如果不可用则阻塞
            semaphore.acquire();
        } catch (InterruptedException e) {
            // 恢复中断状态并抛出运行时异常
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取 WebDriver 被中断", e);
        }
        // 尝试从池中获取一个 WebDriver 实例
        WebDriver driver = pool.poll();
        if (driver != null) {
            return driver;
        }
        // 队列为空但 semaphore 已获取，创建新实例
        try {
            driver = WebScreenshotUtils.initChromeDriver(1600, 900);
            log.info("创建新的 WebDriver 实例");
            return driver;
        } catch (Exception e) {
            // 如果创建失败，释放信号量并抛出异常
            semaphore.release();
            throw new RuntimeException("创建 WebDriver 失败", e);
        }
    }

    /**
     * 归还健康的 WebDriver 到池中
     * @param driver 需要归还的 WebDriver 实例
     */
    public void returnObject(WebDriver driver) {
        if (driver != null) {
            // 将 WebDriver 实例放回池中
            pool.offer(driver);
        }
        // 释放信号量许可
        semaphore.release();
    }

    /**
     * 失效一个 WebDriver（例如发生异常后），不归还池，直接 quit
     * @param driver 需要失效的 WebDriver 实例
     */
    public void invalidate(WebDriver driver) {
        if (driver != null) {
            try {
                // 关闭 WebDriver 实例
                driver.quit();
                log.info("已关闭失效的 WebDriver");
            } catch (Exception e) {
                log.warn("关闭失效 WebDriver 时发生异常", e);
            }
        }
        // 释放信号量许可
        semaphore.release();
    }

    /**
     * 销毁方法，在组件被销毁前自动调用
     * 关闭所有 WebDriver 实例并清理资源
     */
    @PreDestroy
    public void destroy() {
        log.info("开始关闭 WebDriverPool...");
        int count = 0;
        // 循环直到池为空
        while (!pool.isEmpty()) {
            WebDriver driver = pool.poll();
            if (driver != null) {
                try {
                    // 关闭 WebDriver 实例
                    driver.quit();
                    count++;
                } catch (Exception e) {
                    log.warn("关闭 WebDriver 时发生异常", e);
                }
            }
        }
        // 记录清理完成的日志
        log.info("WebDriverPool 已关闭, 共清理 {} 个实例", count);
    }
}
