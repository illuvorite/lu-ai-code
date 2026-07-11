package com.lu.luaicode.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * ClassName: ProjectDownloadService
 * Package: com.lu.luaicode.service
 * Description:
 *
 * @Author Dopamine
 * @Create 2026/7/10 0:03
 * @Version 1.0
 */
public interface ProjectDownloadService {


/**
 * 将指定项目路径下的内容打包成ZIP文件并下载
 *
 * @param projectPath 项目路径，表示需要打包的项目根目录
 * @param downloadFileName 下载时指定的文件名，生成的ZIP文件将以此命名
 * @param response HTTP响应对象，用于将ZIP文件流输出到客户端
 * @return 返回一个String类型的结果，通常表示操作状态或错误信息
 */
void downloadProjectAsZip(String projectPath,String downloadFileName, HttpServletResponse response);
}
