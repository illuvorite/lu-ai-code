package com.lu.luaicode.core;

import cn.hutool.json.JSONUtil;
import com.lu.luaicode.ai.AiCodeGeneratorService;
import com.lu.luaicode.ai.AiCodeGeneratorServiceFactory;
import com.lu.luaicode.ai.model.HtmlCodeResult;
import com.lu.luaicode.ai.model.MultiFileCodeResult;
import com.lu.luaicode.ai.model.message.AiResponseMessage;
import com.lu.luaicode.ai.model.message.ToolExecutedMessage;
import com.lu.luaicode.ai.model.message.ToolRequestMessage;
import com.lu.luaicode.core.parser.CodeParserExecutor;
import com.lu.luaicode.core.saver.CodeFileSaverExecutor;
import com.lu.luaicode.exception.BusinessException;
import com.lu.luaicode.exception.ResultCode;
import com.lu.luaicode.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 * 该类作为AI代码生成的门面模式实现，封装了代码生成和保存的复杂逻辑
 * 提供了同步和异步两种代码生成方式
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory; // 注入AI代码生成服务工厂，用于获取不同类型的AI代码生成服务实例



    /**
     * 同步方法：生成并保存代码
     * @param userMessage 用户输入的消息/需求
     * @param codeGenTypeEnum 代码生成类型枚举
     * @param appId 应用ID，用于标识不同的应用
     * @return 生成的代码文件
     * @throws BusinessException 当生成类型为空或不支持时抛出业务异常
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        if (codeGenTypeEnum == null) { // 检查代码生成类型是否为空
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "生成类型为空");
        }
        // 根据 appId 获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,codeGenTypeEnum);
        return switch (codeGenTypeEnum) { // 使用switch表达式处理不同的代码生成类型
            case HTML -> {
                // 生成HTML代码并保存
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                // 生成多文件代码并保存
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default -> {
                // 不支持的生成类型处理
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ResultCode.INTERNAL_ERROR, errorMessage);
            }
        };
    }


    /**
     * 异步方法：生成并保存代码（流式处理）
     * @param userMessage 用户输入的消息/需求
     * @param codeGenTypeEnum 代码生成类型枚举
     * @param appId 应用ID，用于标识不同的应用
     * @return Flux<String> 流式返回生成的代码
     * @throws BusinessException 当生成类型为空或不支持时抛出业务异常
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        if (codeGenTypeEnum == null) { // 检查代码生成类型是否为空
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "生成类型为空");
        }
        // 根据 appId 获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,codeGenTypeEnum);
        return switch (codeGenTypeEnum) { // 使用switch表达式处理不同的代码生成类型
            case HTML -> {
                // 生成HTML代码流并处理
                Flux<String> stringFlux = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(stringFlux, CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                // 生成多文件代码流并处理
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            case VUE_PROJECT -> {
                TokenStream codeStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId,userMessage);
                yield processTokenStream(codeStream);
            }
            default -> {
                // 不支持的生成类型处理
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ResultCode.INTERNAL_ERROR, errorMessage);
            }
        };
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream
                    .onPartialResponse(token -> {
                        try {
                            sink.next(JSONUtil.toJsonStr(new AiResponseMessage(token)));
                        } catch (Exception e) {
                            log.error("处理流式响应 token 失败", e);
                        }
                    })
                    .beforeToolExecution(before -> {
                        try {
                            sink.next(JSONUtil.toJsonStr(new ToolRequestMessage(before.request())));
                        } catch (Exception e) {
                            log.error("处理工具请求失败", e);
                        }
                    })
                    .onToolExecuted(executed -> {
                        try {
                            sink.next(JSONUtil.toJsonStr(new ToolExecutedMessage(executed)));
                        } catch (Exception e) {
                            log.error("处理工具执行结果失败", e);
                        }
                    })
                    .onCompleteResponse(response -> {
                        log.debug("TokenStream 流式响应完成");
                        sink.complete();
                    })
                    .onError(error -> {
                        log.error("TokenStream 流式响应异常", error);
                        sink.error(error);
                    })
                    .start();

            sink.onCancel(() -> log.debug("TokenStream 下游取消订阅，释放资源"));
            sink.onDispose(() -> log.debug("TokenStream FluxSink 已释放"));
        });
    }



    /**
     * 处理代码流的方法
     * 该方法接收一个代码流，实时收集代码片段，并在流式返回完成后保存代码
     * @param codeStream 代码流
     * @param codeGenType 代码生成类型
     * @param appId 应用ID，用于标识不同的应用
     * @return 处理后的代码流
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType,Long appId) {
        StringBuilder codeBuilder = new StringBuilder(); // 用于收集代码片段的字符串构建器
        return codeStream.doOnNext(chunk -> {
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式返回完成后保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                // 使用执行器保存代码
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType,appId);
                log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }


}
