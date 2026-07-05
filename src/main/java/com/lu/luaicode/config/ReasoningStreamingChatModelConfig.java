package com.lu.luaicode.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: ResoningStreamingChatModelConfig
 * Package: com.lu.luaicode.config
 * Description:
 *
 * @Author Dopamine
 * @Create 2026/7/5 17:23
 * @Version 1.0
 */
@Configuration
@Slf4j
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat.model")
public class ReasoningStreamingChatModelConfig {


    private String baseUrl;
    private String apiKey;

    /**
     * 推理流式模型用于生成Vue项目带工具调用
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        final String modelName="deepseek-flash";
        final int maxToken=10000;
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
