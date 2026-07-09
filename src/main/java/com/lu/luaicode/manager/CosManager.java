package com.lu.luaicode.manager;


import cn.hutool.core.io.FileUtil;
import com.lu.luaicode.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯云对象存储服务管理器
 * 提供文件上传、下载、删除等功能，并支持图片处理和存储桶配置
 */
@Component
@Tag(name = "腾讯云对象存储服务")
@RequiredArgsConstructor
@Slf4j
public class CosManager {

    /**
     * COS客户端配置
     */
    private final CosClientConfig cosClientConfig;

    /**
     * COS客户端实例
     */
    private final COSClient cosClient;

    /**
     * 上传文件到COS
     * @param key 文件在存储桶中的键
     * @param file 要上传的文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 从COS下载文件
     * @param key 文件在存储桶中的键
     * @return COS对象
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    public String uploadFile(String key, File file) {
        PutObjectResult result = putObject(key, file);
        if (result != null) {
            String url=String.format("%s%s",cosClientConfig.getHost(),key);
            log.info("文件上传COS成功: {}->{}", file.getName(),url);
            return url;
        }else {
            log.error("文件上传COS失败: {}", file.getName());
            return null;
        }
    }


}
