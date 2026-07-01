package com.lu.luaicode.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lu.luaicode.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AiCodeGeneratorServiceFactory 类是一个配置类，用于创建和配置 AiCodeGeneratorService 的 Bean。
 * 通过 @Configuration 注解标记为 Spring 配置类。
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;  // 注入 ChatModel 类型的依赖，用于与 AI 模型进行交互

    @Resource
    private StreamingChatModel streamingChatModel;  // 注入 StreamingChatModel 类型的依赖，用于与 AI 模型进行交互

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;  // 注入 RedisChatMemoryStore 类型的依赖，用于与 Redis 进行交互

    @Resource
    private ChatHistoryService chatHistoryService;  // 注入 ChatHistoryService 类型的依赖，用于保存对话历史



    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)  // 设置缓存最大容量为 1000
            .expireAfterWrite(Duration.ofMinutes(30))  // 设置写入后过期时间为 30 分钟
            .expireAfterAccess(Duration.ofMinutes(10))  // 设置访问后过期时间为 10 分钟
            .removalListener((key, value, cause) -> {  // 添加缓存移除监听器
                log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
            })
            .build();  // 构建缓存实例

    /**
     * 根据 appId 获取服务（带缓存）
     * @param appId 应用 ID
     * @return 返回对应的 AiCodeGeneratorService 实例
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId) {
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }

    /**
     * 创建新的 AI 服务实例
     * @param appId 应用 ID
     * @return 返回新创建的 AiCodeGeneratorService 实例
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(Long appId) {
        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)  // 设置对话记忆 ID 为 appId
                .chatMemoryStore(redisChatMemoryStore)  // 设置 Redis 存储器
                .maxMessages(20)  // 设置最大消息数量为 20
                .build();  // 构建对话记忆实例
        chatHistoryService.loadChatHistoryToMemory(appId,chatMemory,20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)  // 设置聊天模型
                .streamingChatModel(streamingChatModel)  // 设置流式聊天模型
                .chatMemory(chatMemory)  // 设置对话记忆
                .build();  // 构建 AI 服务实例
    }


    /**
     * 创建并配置 StreamingAiCodeGeneratorService 的 Bean。
     * 使用 @Bean 注解将该方法返回的对象注册为 Spring 容器中的 Bean。
     *
     * @return 返回一个 StreamingAiCodeGeneratorService 实例，该实例使用注入的 StreamingChatModel 进行初始化。
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L);  // 使用默认的 appId (0L) 获取服务实例
    }
}
