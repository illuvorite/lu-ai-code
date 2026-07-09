package com.lu.luaicode.service;


public interface ScreenshotService {

    /**
     * 截图
     * @param webUrl
     * @return
     */
    String generateAndUploadScreenshot(String webUrl);

}
