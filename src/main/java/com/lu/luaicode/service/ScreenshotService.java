package com.lu.luaicode.service;


/**
 * 截图服务接口
 * 该接口定义了生成并上传截图的方法
 */
public interface ScreenshotService {

    /**
     * 截图
     * @param webUrl
     * @return
     */
    String generateAndUploadScreenshot(String webUrl);

}
