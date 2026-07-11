package com.lu.luaicode.langgraph4j.node;

import com.lu.luaicode.constant.AppConstant;
import com.lu.luaicode.core.AiCodeGeneratorFacade;
import com.lu.luaicode.langgraph4j.state.WorkflowContext;
import com.lu.luaicode.model.enums.CodeGenTypeEnum;
import com.lu.luaicode.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 代码生成器节点类
 * 用于处理代码生成流程的异步节点操作
 */
@Slf4j
public class CodeGeneratorNode {

    /**
     * 创建代码生成器节点
     * @return 返回一个异步节点操作，用于处理代码生成流程
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 获取当前工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            // 记录节点执行日志
            log.info("执行节点: 代码生成");

            // 使用增强提示词作为发给 AI 的用户消息
            String userMessage = context.getEnhancedPrompt();
            // 获取代码生成类型
            CodeGenTypeEnum generationType = context.getGenerationType();
            // 获取 AI 代码生成外观服务
            AiCodeGeneratorFacade codeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            // 记录代码生成开始日志
            log.info("开始生成代码，类型: {} ({})", generationType.getValue(), generationType.getText());
            // 先使用固定的 appId (后续再整合到业务中)
            Long appId = 0L;
            // 调用流式代码生成
            Flux<String> codeStream = codeGeneratorFacade.generateAndSaveCodeStream(userMessage, generationType, appId);
            // 同步等待流式输出完成
            codeStream.blockLast(Duration.ofMinutes(10)); // 最多等待 10 分钟
            // 根据类型设置生成目录
            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR, generationType.getValue(), appId);
            log.info("AI 代码生成完成，生成目录: {}", generatedCodeDir);

            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }
}

