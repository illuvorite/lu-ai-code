package com.lu.luaicode.core.saver;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.lu.luaicode.exception.ResultCode;
import com.lu.luaicode.exception.ThrowUtils;
import com.lu.luaicode.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象类 CodeFileSaverTemplate，作为文件保存的模板类
 * 这是一个泛型抽象类，使用类型参数 T 表示要保存的数据类型
 *
 * @param <T> 泛型类型参数，表示可以保存任意类型的数据
 */
public abstract class CodeFileSaverTemplate<T> {

    // 文件保存根目录，使用系统当前工作目录下的 tmp/code_output 目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    // 这是一个抽象类，可以作为保存文件操作的模板
    // 具体的保存逻辑需要由子类实现

    /**
     * 保存代码并返回文件目录对象
     * @param result 需要保存的结果对象
     * @return 保存结果的文件目录对象
     */
    public final File saveCode(T result) {
        //验证输入参数的有效性
        validateInput(result);
        //构建唯一目录
        String baseDirPath = buildUniqueDir();
        //保存文件
        saveFiles(result, baseDirPath);
        // 调用抽象方法，由子类实现具体的保存逻辑
        //返回文件目录对象
        return new File(baseDirPath);
    }



/**
 * 验证输入参数的方法
 * @param result 需要验证的输入参数，类型为泛型T
 * @throws 如果result为null，则抛出带有指定错误码和消息的异常
 */
    protected void validateInput(T result) {
        // 使用ThrowUtils工具类检查result是否为null
        // 如果为null，则抛出带有ResultCode.NOT_FOUND错误码和"result is null"消息的异常
        ThrowUtils.throwIf(result == null, ResultCode.NOT_FOUND, "result is null");
    }

/**
 * 构建唯一的目录名称
 * 该方法通过业务类型和雪花算法生成的ID创建一个唯一的目录路径
 *
 * @return 返回创建好的目录路径字符串
 */
    protected   String buildUniqueDir() {
        // 获取当前代码类型的值
        String codeType=getCodeType().getValue();
        // 使用业务类型和雪花算法生成的ID拼接成唯一的目录名称
        String uniqueDirName = StrUtil.format("{}_{}", codeType, IdUtil.getSnowflakeNextIdStr());
        // 拼接完整的目录路径，使用系统相关的文件分隔符
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        // 创建目录，如果目录已存在则不创建，不存在则创建
        FileUtil.mkdir(dirPath);
        // 返回创建好的目录路径
        return dirPath;
    }



/**
 * 将指定内容写入到文件中
 * @param dirPath 文件所在目录路径
 * @param filename 文件名
 * @param content 要写入文件的内容
 */
    public final  void writeToFile(String dirPath, String filename, String content) {
    // 检查内容是否为空或空白字符串
        if (StrUtil.isNotBlank(content)){
            // 构建完整的文件路径，使用系统相关的路径分隔符
            String filePath = dirPath + File.separator + filename;
            // 使用FileUtil工具类将内容以UTF-8编码写入指定文件
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }

    }
/**
 * 获取代码生成类型的抽象方法
 * 这是一个抽象方法，由子类实现以返回具体的代码生成类型
 *
 * @return 返回代码生成类型的枚举值
 */
protected abstract CodeGenTypeEnum getCodeType();
/**
 * 保护抽象方法，用于保存文件
 * @param result 需要保存的结果对象
 * @param baseDirPath 文件保存的基础目录路径
 */
    protected abstract void saveFiles(T result, String baseDirPath) ;
}
