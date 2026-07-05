package com.lu.luaicode.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lu.luaicode.ai.tools.FileWriteTool;
import com.lu.luaicode.exception.BusinessException;
import com.lu.luaicode.exception.ResultCode;
import com.lu.luaicode.model.enums.CodeGenTypeEnum;
import com.lu.luaicode.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
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
    private StreamingChatModel openAiStreamingChatModel;  // 注入 StreamingChatModel 类型的依赖，用于与 AI 模型进行交互

    @Resource
    private StreamingChatModel reasoningStreamingChatModel;

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
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)  // 设置缓存最大容量为 1000
            .expireAfterWrite(Duration.ofMinutes(30))  // 设置写入后过期时间为 30 分钟
            .expireAfterAccess(Duration.ofMinutes(10))  // 设置访问后过期时间为 10 分钟
            .removalListener((key, value, cause) -> {  // 添加缓存移除监听器
                log.debug("AI 服务实例被移除，缓存键: {}, 原因: {}", key, cause);
            })
            .build();  // 构建缓存实例

    /**
     * 根据 appId 获取服务（带缓存）
     * @param appId 应用 ID
     * @return 返回对应的 AiCodeGeneratorService 实例
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }


    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenType) {
        String cacheKey=buildCacheKey(appId,codeGenType);
        return serviceCache.get(cacheKey,key -> createAiCodeGeneratorService(appId,codeGenType));
    }

    /**
     * 创建新的 AI 服务实例
     * @param appId 应用 ID
     * @return 返回新创建的 AiCodeGeneratorService 实例
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenType) {
        log.info("为 appId: {} 创建新的 AI 服务实例", appId);  // 记录日志，表示正在为特定 appId 创建 AI 服务实例
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory  // 创建 MessageWindowChatMemory 实例
                .builder()  // 使用构建器模式创建实例
                .id(appId)  // 设置对话记忆 ID 为 appId，确保每个应用有独立的对话记忆
                .chatMemoryStore(redisChatMemoryStore)  // 设置 Redis 存储器，用于持久化对话记忆
                .maxMessages(20)  // 设置最大消息数量为 20，控制上下文长度
                .build();  // 构建对话记忆实例
        chatHistoryService.loadChatHistoryToMemory(appId,chatMemory,20);  // 从 Redis 加载历史对话到内存中
        return switch (codeGenType){
            case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)  // 使用构建器模式创建 AI 服务实例
                        .chatModel(chatModel)  // 设置聊天模型，用于非流式对话
                        .streamingChatModel(reasoningStreamingChatModel)  // 设置流式聊天模型，用于流式对话
                        .chatMemoryProvider(memory ->chatMemory)// 设置对话记忆，保持对话上下文
                        .tools(new FileWriteTool())
                        //处理工具幻觉问题
                        .hallucinatedToolNameStrategy(toolExecutionRequest ->
                                ToolExecutionResultMessage.from(toolExecutionRequest,"Error: there is no tool called"+
                                        toolExecutionRequest.name())
                                )
                        .build();  // 构建 AI 服务实例
            case HTML,MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)  // 使用构建器模式创建 AI 服务实例
                        .chatModel(chatModel)  // 设置聊天模型，用于非流式对话
                        .streamingChatModel(openAiStreamingChatModel)  // 设置流式聊天模型，用于流式对话
                        .chatMemory(chatMemory)  // 设置对话记忆，保持对话上下文
                        .build();  // 构建 AI 服务实例

            default -> throw new BusinessException(ResultCode.PARAM_ERROR,"不支持的代码生成类型"+codeGenType.getValue());
        };
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

    private String buildCacheKey(Long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();  // 使用 appId 和 codeGenType 构建缓存键
    }
}
