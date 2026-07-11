package com.lu.luaicode.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.lu.luaicode.exception.BusinessException;
import com.lu.luaicode.exception.ResultCode;
import com.lu.luaicode.exception.ThrowUtils;
import com.lu.luaicode.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

/**
 * ClassName: ProjectDownloadServiceImpl
 * Package: com.lu.luaicode.service.impl
 * Description:
 *
 * @Author Dopamine
 * @Create 2026/7/10 0:03
 * @Version 1.0
 */
@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    /**
     * 需要过滤的文件和目录名称
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );
    /**
     * 需要过滤的文件扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );


    /**
     * 将项目目录打包成ZIP文件并下载
 *
     * @param projectPath 项目路径，表示需要打包的项目根目录
     * @param downloadFileName 下载时指定的文件名，生成的ZIP文件将以此命名
     * @param response HTTP响应对象，用于将ZIP文件流输出到客户端
     * @return 返回空字符串，此方法主要通过response输出文件流
     */
    @Override
    public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response) {
        //基础校验：检查参数是否合法
        ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ResultCode.PARAM_ERROR, "项目路径不能为空"); // 检查项目路径是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(downloadFileName), ResultCode.PARAM_ERROR, "下载文件名不能为空"); // 检查下载文件名是否为空
    // 检查项目目录是否存在且有效
        File projectDir=new File(projectPath); // 创建文件对象
        ThrowUtils.throwIf(!projectDir.exists(), ResultCode.PARAM_ERROR, "项目路径不存在"); // 检查目录是否存在
        ThrowUtils.throwIf(!projectDir.isDirectory(), ResultCode.PARAM_ERROR, "项目路径不是一个目录"); // 检查是否为目录
    // 记录开始打包的日志
        log.info("开始打包项目，项目路径: {}", projectPath); // 记录打包开始日志

        //设置http响应头
        response.setStatus(HttpServletResponse.SC_OK); // 设置响应状态码为200
        response.setContentType("application/zip"); // 设置响应内容类型为ZIP
        response.addHeader("Content-Disposition",String.format("attachment; filename=\"%s.zip\"",downloadFileName)); // 设置下载文件名
        //定义文件过滤器
        FileFilter fileFilter = file -> isPathAllowed(projectDir.toPath(),file.toPath()); // 创建文件过滤器，用于控制哪些文件可以被包含在ZIP中
        //压缩并下载
        try {
            //使用hutool工具库进行ZIP压缩
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8,false,fileFilter,projectDir); // 执行ZIP压缩操作
            log.info("打包项目下载成功:{}->{}.zip",projectPath,downloadFileName); // 记录成功日志
        } catch (IOException e) {
            log.error("打包项目失败",e); // 记录错误日志
            throw new BusinessException(ResultCode.DATA_OPERATION_FAIL,"项目打包失败"); // 抛出业务异常
        }
    }

    private boolean isPathAllowed(Path projectRoot,Path fullPath) {
        Path relativize = projectRoot.relativize(fullPath);
        for (Path part: relativize) {
            String partName = part.toString();
            if (IGNORED_NAMES.contains(partName)) {
                return false;
            }

            if (IGNORED_EXTENSIONS.stream().anyMatch(ext->partName.toLowerCase().endsWith(ext))) {
                return false;
            }
        }
        return true;
    }
}
