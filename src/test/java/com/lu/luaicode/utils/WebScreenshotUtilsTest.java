package com.lu.luaicode.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassName: WebScreenshotUtilsTest
 * Package: com.lu.luaicode.utils
 * Description:
 *
 * @Author Dopamine
 * @Create 2026/7/9 12:22
 * @Version 1.0
 */
@SpringBootTest
@Slf4j
class WebScreenshotUtilsTest {

    @Test
    void saveWebPageScreenshot() {
        String url = "https://www.baidu.com";
        //String screenshotPath = WebScreenshotUtils.saveWebPageScreenshot(url);
        //assertNotNull(screenshotPath);
    }
}