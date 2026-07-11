package com.lu.luaicode.langgraph4j;

import com.lu.luaicode.exception.BusinessException;
import com.lu.luaicode.exception.ResultCode;
import com.lu.luaicode.langgraph4j.model.QualityResult;
import com.lu.luaicode.langgraph4j.node.*;
import com.lu.luaicode.langgraph4j.state.WorkflowContext;
import com.lu.luaicode.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 工作流应用程序主类
 * 使用了@Slf4j注解进行日志记录
 */
@Slf4j
public class CodeGenWorkflow {

    /**
     * 创建完整的工作流
     * @return 编译后的工作流对象
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            // 创建消息状态图并添加各个处理节点
            return new MessagesStateGraph<String>()
                    // 添加节点 - 使用完整实现的节点
                    .addNode("image_collector", ImageCollectorNode.create())      // 添加图像收集器节点
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())      // 添加提示词增强器节点
                    .addNode("router", RouterNode.create())                      // 添加路由节点
                    .addNode("code_generator", CodeGeneratorNode.create())        // 添加代码生成器节点
                    .addNode("code_quality_check",CodeQualityCheckNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())      // 添加项目构建器节点

                    // 添加边，定义节点间的执行顺序
                    .addEdge(START, "image_collector")                           // 从起始节点到图像收集器
                    .addEdge("image_collector", "prompt_enhancer")                // 图像收集器到提示词增强器
                    .addEdge("prompt_enhancer", "router")                        // 提示词增强器到路由节点
                    .addEdge("router", "code_generator")                         // 路由节点到代码生成器
                    .addNode("code_quality_check", CodeQualityCheckNode.create())
                    .addEdge("code_generator", "code_quality_check")
                    // 新增质检条件边：根据质检结果决定下一步
                    .addConditionalEdges("code_quality_check",
                            edge_async(this::routeAfterQualityCheck),
                            Map.of(
                                    "build", "project_builder",   // 质检通过且需要构建
                                    "skip_build", END,            // 质检通过但跳过构建
                                    "fail", "code_generator"      // 质检失败，重新生成
                            ))

                    .addEdge("project_builder", END)                             // 项目构建器到结束节点

                    // 编译工作流
                    .compile();
        } catch (GraphStateException e) {
            // 捕获并处理图状态异常
            throw new BusinessException(ResultCode.DATA_OPERATION_FAIL, "工作流创建失败");
        }
    }
    private String routeBuildOrSkip(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        CodeGenTypeEnum generationType = context.getGenerationType();
        // HTML 和 MULTI_FILE 类型不需要构建，直接结束
        if (generationType == CodeGenTypeEnum.HTML || generationType == CodeGenTypeEnum.MULTI_FILE) {
            return "skip_build";
        }
        // VUE_PROJECT 需要构建
        return "build";
    }

    /**
     * 执行工作流
     * @param originalPrompt 原始提示词
     * @return 工作流执行完成后的上下文
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        // 创建工作流实例
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        // 初始化 WorkflowContext
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt(originalPrompt)
                .currentStep("初始化")
                .build();

        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());
        log.info("开始执行代码生成工作流");

        WorkflowContext finalContext = null;
        int stepCounter = 1;
        for (NodeOutput<MessagesState<String>> step : workflow.stream(
                Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            // 显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                finalContext = currentContext;
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }
        log.info("代码生成工作流执行完成！");
        return finalContext;
    }
    private String routeAfterQualityCheck(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        QualityResult qualityResult = context.getQualityResult();
        // 如果质检失败，重新生成代码
        if (qualityResult == null || !qualityResult.getIsValid()) {
            log.error("代码质检失败，需要重新生成代码");
            return "fail";
        }
        // 质检通过，使用原有的构建路由逻辑
        log.info("代码质检通过，继续后续流程");
        return routeBuildOrSkip(state);
    }

}

