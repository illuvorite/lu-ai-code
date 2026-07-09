package com.lu.luaicode.ai.tools;

import com.lu.luaicode.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文件写入工具
 * 支持 AI 通过工具调用的方式写入文件
 *
 * 该工具提供了以下功能：
 * 1. 将指定内容写入到指定路径的文件中
 * 2. 自动创建不存在的目录
 * 3. 支持相对路径和绝对路径
 * 4. 记录文件写入统计信息
 * 5. 提供详细的写入结果反馈
 */
@Slf4j
public class FileWriteTool {

    // 用于记录每个应用ID的文件写入计数
    // 使用ConcurrentHashMap保证线程安全
    private static final ConcurrentHashMap<Long, AtomicInteger> fileCounters = new ConcurrentHashMap<>();

    @Tool("写入文件到指定路径")
    public String writeFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @P("要写入文件的内容")
            String content,
            @ToolMemoryId Long appId
    ) {
        try {
            Path path = Paths.get(relativeFilePath);
            if (!path.isAbsolute()) {
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(relativeFilePath);
            }
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            Files.write(path, content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            int fileCount = fileCounters.computeIfAbsent(appId, k -> new AtomicInteger(0)).incrementAndGet();
            long fileSize = content.length();
            log.info("成功写入文件({}/{}): {}", fileCount, relativeFilePath, path.toAbsolutePath());

            return String.format("文件写入成功 [第%d个文件, 大小:%d字符]: %s", fileCount, fileSize, relativeFilePath);
        } catch (IOException e) {
            String errorMessage = "文件写入失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
}
