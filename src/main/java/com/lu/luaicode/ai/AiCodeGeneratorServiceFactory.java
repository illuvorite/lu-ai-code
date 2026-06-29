package com.lu.luaicode.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AiCodeGeneratorServiceFactory 类是一个配置类，用于创建和配置 AiCodeGeneratorService 的 Bean。
 * 通过 @Configuration 注解标记为 Spring 配置类。
 */
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;  // 注入 ChatModel 类型的依赖，用于与 AI 模型进行交互

    @Resource
    private StreamingChatModel streamingChatModel;  // 注入 StreamingChatModel 类型的依赖，用于与 AI 模型进行交互

    /**
     * 创建并配置 StreamingAiCodeGeneratorService 的 Bean。
     * 使用 @Bean 注解将该方法返回的对象注册为 Spring 容器中的 Bean。
     *
     * @return 返回一个 StreamingAiCodeGeneratorService 实例，该实例使用注入的 StreamingChatModel 进行初始化。
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
