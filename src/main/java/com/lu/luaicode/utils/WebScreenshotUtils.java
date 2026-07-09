package com.lu.luaicode.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.lu.luaicode.exception.BusinessException;
import com.lu.luaicode.exception.ResultCode;
import com.lu.luaicode.pool.WebDriverPool;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.util.UUID;


/**
 * Web截图工具类，提供网页截图、图片压缩、临时文件清理等功能
 */
@Slf4j
@Component
public class WebScreenshotUtils {

    @Resource
    private WebDriverPool webDriverPool;

/**
 * 保存网页截图并压缩
 * @param webUrl 要截图的网页URL地址
 * @return 返回压缩后的图片路径，如果失败则返回null
 */
    public String saveWebPageScreenshot(String webUrl) {
        //非空校验
        if(StrUtil.isBlank(webUrl)){
            log.error("网页地址为空,截图失败");
            return null;
        }
        // 从WebDriver池中获取一个WebDriver实例
        WebDriver driver = webDriverPool.borrow();
        boolean success = false;
        try {
            //创建临时目录，路径包含UUID前8位作为唯一标识
            String rootPath = System.getProperty("user.dir") + "/tmp/screenshots"+"/screenshot"+ UUID.randomUUID().toString().substring(0,8);
            FileUtil.mkdir(rootPath);
            //图片后缀
            final String IMAGE_SUFFIX = ".png";
            //原始图片保存路径
            String imageSavePath=rootPath+ File.separator+ RandomUtil.randomNumbers(5)+IMAGE_SUFFIX;
            //访问网页
            driver.get(webUrl);
            //等待网页加载
            waitForPageLoad(driver);
            //截图
            byte[] screenshotBytes =((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            //保存原始图片
            saveImage(screenshotBytes, imageSavePath);
            log.info("网页截图成功,截图路径:{}",imageSavePath);
            //压缩图片
            final String COMPRESSED_SUFFIX = "_compressed.jpg";
            String compressedImagePath = rootPath+ File.separator+ RandomUtil.randomNumbers(5)+COMPRESSED_SUFFIX;
            compressImage(imageSavePath, compressedImagePath);
            log.info("图片压缩成功,压缩图片路径:{}",compressedImagePath);
            //删除原始图片
            FileUtil.del(imageSavePath);
            success = true;
            return compressedImagePath;
        } catch (Exception e) {
            webDriverPool.invalidate(driver);
            log.error("图片压缩失败", e);
            return null;
        } finally {
            if (success) {
                webDriverPool.returnObject(driver);
            }
        }
    }


    /**
     * 初始化 Chrome 浏览器驱动
     * 配置了无头模式、窗口大小、用户代理等参数
     * @param width 浏览器窗口宽度
     * @param height 浏览器窗口高度
     * @return 配置好的WebDriver实例
     */
    public static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ResultCode.PARAM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    /**
     * 保存图片到指定路径
     * @param imageBytes 图片字节数组
     * @param imagePath 图片保存路径
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存截图失败", e);
            throw new BusinessException(ResultCode.PARAM_ERROR, "保存截图失败");
        }
    }

    /**
     * 压缩图片
     * 使用ImgUtil工具类将图片压缩为JPEG格式，质量为0.3
     * @param originImagePath 原始图片路径
     * @param compressedImagePath 压缩后图片保存路径
     */
    private static void compressImage(String originImagePath, String compressedImagePath) {
        // 设置压缩质量为0.3
        final float COMPRESSION_QUALITY = 0.3f;
        try {
            // 使用ImgUtil工具类压缩图片
            ImgUtil.compress(
                    FileUtil.file(originImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY
            );
        } catch (Exception e) {
            log.error("压缩图片失败:{}->{}",originImagePath,compressedImagePath, e);
            throw new BusinessException(ResultCode.PARAM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待页面完全加载
     * 通过JavaScript检查document.readyState，并额外等待1秒确保动态内容加载完成
     * @param driver 浏览器驱动对象
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {
            //等待页面加载
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            //等待document.readyState为complete
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState")
                    .equals("complete")
            );
            //额外等待一段时间确保动态内容加载完成
            Thread.sleep(1000);
            log.info("页面加载完成");
        }catch (Exception e){
            log.error("等待页面加载失败", e);
            throw new BusinessException(ResultCode.PARAM_ERROR, "等待页面加载失败");
        }

    }


    /**
     * 清理24小时前的临时截图目录
     * 该方法会检查用户目录下的tmp/screenshots/screenshot文件夹，
     * 删除其中所有超过24小时未被修改的子目录
     */
    public static void cleanTempFiles() {
        // 构建临时截图目录路径，使用当前工作目录下的tmp/screenshots/screenshot
        String basePath = System.getProperty("user.dir") + "/tmp/screenshots"+"/screenshot";
        // 创建File对象表示截图目录
        File screenshotsDir = new File(basePath);
        // 检查目录是否存在以及是否为目录
        if (!screenshotsDir.exists() || !screenshotsDir.isDirectory()) {
            log.info("截图临时目录不存在，跳过清理: {}", basePath);
            return;
        }

        // 计算24小时前的时间戳（毫秒）
        long cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L;
        // 获取所有子目录
        File[] subDirs = screenshotsDir.listFiles(File::isDirectory);
        // 检查是否有子目录需要清理
        if (subDirs == null || subDirs.length == 0) {
            log.info("没有需要清理的过期截图目录");
            return;
        }

        // 初始化计数器，记录已删除的目录数量
        int deleteCount = 0;
        // 遍历所有子目录
        for (File dir : subDirs) {
            // 检查目录是否超过24小时未被修改
            if (dir.lastModified() < cutoff) {
                try {
                    // 删除过期目录
                    FileUtil.del(dir);
                    // 记录删除日志
                    log.info("已清理过期截图目录: {} (最后修改: {})", dir.getAbsolutePath(), new java.util.Date(dir.lastModified()));
                    // 增加删除计数
                    deleteCount++;
                } catch (Exception e) {
                    // 记录删除失败的错误日志
                    log.error("清理截图目录失败: {}", dir.getAbsolutePath(), e);
                }
            }
        }
        // 记录清理完成的总结日志
        log.info("截图清理完成，共清理 {} 个过期目录", deleteCount);
    }

}

