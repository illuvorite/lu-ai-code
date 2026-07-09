package com.lu.luaicode.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 推理流式模型配置，用于生成Vue项目（带工具调用）
 */
@Configuration
@Slf4j
public class ReasoningStreamingChatModelConfig {

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        log.info("创建推理流式模型，baseUrl: {}, apiKey: {}...", baseUrl, apiKey.substring(0, 8));
        final String modelName = "deepseek-v4-flash";
        final int maxToken = 10000;
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxToken)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

}
