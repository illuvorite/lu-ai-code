package com.lu.luaicode.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.lu.luaicode.ai.model.HtmlCodeResult;
import com.lu.luaicode.ai.model.MultiFileCodeResult;
import com.lu.luaicode.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * CodeFileSaver 类用于保存生成的代码文件到本地文件系统
 * 支持保存HTML代码和多文件代码结果
 */
@Deprecated
public class CodeFileSaver {

    // 文件保存根目录，使用系统当前工作目录下的 tmp/code_output 目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存 HtmlCodeResult
     * 该方法用于将生成的HTML代码保存到本地文件系统中
     *
     * @param result 包含HTML代码的结果对象，其中包含需要保存的HTML内容
     * @return 返回保存后的文件对象，指向保存HTML文件的目录
     */
    public static File saveHtmlCodeResult(HtmlCodeResult result) {
        // 构建基于代码生成类型(HTML)的唯一目录路径
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        // 将HTML代码写入到指定目录下的index.html文件中
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        // 返回创建的目录对应的File对象
        return new File(baseDirPath);
    }

    /**
     * 保存 MultiFileCodeResult
     * 该方法用于将包含HTML、CSS和JS代码的多文件代码结果保存到指定目录中
     *
     * @param result 包含HTML、CSS和JS代码的MultiFileCodeResult对象
     * @return 返回保存后的文件对象，指向创建的目录
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult result) {
        // 构建基于代码生成类型唯一标识的目录路径
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        // 将HTML代码写入到index.html文件中
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        // 将CSS代码写入到style.css文件中
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        // 将JavaScript代码写入到script.js文件中
        writeToFile(baseDirPath, "script.js", result.getJsCode());
        // 返回创建的目录文件对象
        return new File(baseDirPath);
    }

    /**
     * 构建唯一目录路径：tmp/code_output/bizType_雪花ID
     * 该方法用于创建一个基于业务类型和雪花算法生成的唯一ID的目录路径
     *
     * @param bizType 业务类型标识符，用于区分不同业务的输出目录
     * @return 返回构建好的完整目录路径字符串
     *         目录路径格式为：FILE_SAVE_ROOT_DIR + File.separator + bizType_雪花ID
     */
    private static String buildUniqueDir(String bizType) {
        // 使用业务类型和雪花算法生成的ID拼接成唯一的目录名称
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        // 拼接完整的目录路径，使用系统相关的文件分隔符
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        // 创建目录，如果目录已存在则不创建，不存在则创建
        FileUtil.mkdir(dirPath);
        // 返回创建好的目录路径
        return dirPath;
    }

    /**
     * 写入单个文件
 * 这是一个私有静态方法，用于将指定内容写入到指定路径的文件中
 *
 * @param dirPath 文件所在的目录路径
 * @param filename 要写入的文件名
 * @param content 要写入文件的内容
     */
    private static void writeToFile(String dirPath, String filename, String content) {
    // 构建完整的文件路径，使用系统相关的路径分隔符
        String filePath = dirPath + File.separator + filename;
    // 使用FileUtil工具类将内容以UTF-8编码写入指定文件
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}
