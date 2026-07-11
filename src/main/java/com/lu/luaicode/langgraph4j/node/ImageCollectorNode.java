package com.lu.luaicode.langgraph4j.node;

import com.lu.luaicode.langgraph4j.ai.ImageCollectionService;
import com.lu.luaicode.langgraph4j.ai.ImageCollectionServiceFactory;
import com.lu.luaicode.langgraph4j.state.ImageCategoryEnum;
import com.lu.luaicode.langgraph4j.state.ImageResource;
import com.lu.luaicode.langgraph4j.state.WorkflowContext;
import com.lu.luaicode.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片收集节点类
 * 使用@Slf4j注解提供日志功能
 */
@Slf4j
public class ImageCollectorNode {



    /**
     * 创建一个异步节点动作，用于执行图片收集任务
     * @return 返回一个AsyncNodeAction类型的函数式接口实现
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 获取当前工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            // 记录执行日志
            log.info("执行节点: 图片收集");

            // 获取原始提示词
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr="";
            try {
                // 调用图片收集 AI 服务
                ImageCollectionService imageCollectionService= SpringContextUtil.getBean(ImageCollectionService.class);
                //使用ai服务进行智能图片收集
                imageListStr=imageCollectionService.collectImages(originalPrompt);
                imageCollectionService.collectImages(originalPrompt);
            } catch (Exception e) {
                log.error("图片收集失败: {}", e.getMessage(), e);
            }
            
            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageListStr);
            return WorkflowContext.saveContext(context);
        });
    }
}
