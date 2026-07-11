package com.lu.luaicode.langgraph4j.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lu.luaicode.langgraph4j.state.ImageCategoryEnum;
import com.lu.luaicode.langgraph4j.state.ImageResource;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片搜索工具类
 * 用于通过Pexels API搜索相关图片资源，用于网站内容展示
 */
@Slf4j
@Component
public class ImageSearchTool {

    // Pexels API的基础URL
    private static final String PEXELS_API_URL = "https://api.pexels.com/v1/search";

    // 从配置文件中注入的Pexels API密钥
    @Value("${pexels.api-key}")
    private String pexelsApiKey;

    /**
     * 搜索内容相关的图片
     * @param query 搜索关键词
     * @return 图片资源列表
     */
    @Tool("搜索内容相关的图片，用于网站内容展示")
    public List<ImageResource> searchContentImages(@P("搜索关键词") String query) {
        // 初始化图片列表
        List<ImageResource> imageList = new ArrayList<>();
        // 设置每页返回的图片数量
        int searchCount = 4;
        // 调用 API，注意释放资源
        try (HttpResponse response = HttpRequest.get(PEXELS_API_URL)
                .header("Authorization", pexelsApiKey)
                .form("query", query)
                .form("per_page", searchCount)
                .form("page", 1)
                .execute()) {
            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                JSONArray photos = result.getJSONArray("photos");
                for (int i = 0; i < photos.size(); i++) {
                    JSONObject photo = photos.getJSONObject(i);
                    JSONObject src = photo.getJSONObject("src");
                    imageList.add(ImageResource.builder()
                            .category(ImageCategoryEnum.CONTENT)
                            .description(photo.getStr("alt", query))
                            .url(src.getStr("medium"))
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("Pexels API 调用失败: {}", e.getMessage(), e);
        }
        return imageList;
    }
}
