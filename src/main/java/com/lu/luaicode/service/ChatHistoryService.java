package com.lu.luaicode.service;


import com.lu.luaicode.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.lu.luaicode.model.dto.entity.ChatHistory;
import com.lu.luaicode.model.dto.entity.User;
import com.lu.luaicode.model.vo.ChatHistoryVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author illusory
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存对话消息
     *
     * @param appId       应用 ID
     * @param userId      用户 ID
     * @param message     消息内容
     * @param messageType 消息类型（user/ai）
     */
    boolean saveMessage(Long appId, Long userId, String message, String messageType);

    /**
     * 分页查询应用的对话历史（仅应用创建者和管理员可查看）
     *
     * @param loginUser 当前登录用户
     * @return 分页结果
     */
    Page<ChatHistory> getChatHistoryPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);


    /**
     * 级联删除应用的对话历史
     *
     * @param appId 应用 ID
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteByAppId(Long appId);


    /**
     * 导出应用的对话历史为 Markdown 文件（流式写入）
     *
     * @param appId    应用 ID
     * @param response HTTP 响应（直接写入输出流）
     * @param loginUser 当前登录用户
     */
    void exportChatHistoryToMarkdown(Long appId, HttpServletResponse response, User loginUser);

/**
 * 将聊天历史记录加载到内存中
 *
 * @param appId 应用程序ID，用于标识特定的应用程序
 * @param maxCount 最大加载记录数量，限制加载的历史记录条数
 * @return 返回实际加载的记录数量，可能是0到maxCount之间的值
 */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

/**
 * 根据聊天历史查询请求参数获取查询构造器
 * @param chatHistoryQueryRequest 聊天历史查询请求对象，包含查询条件
 * @return QueryWrapper 返回一个用于构建数据库查询条件的QueryWrapper对象
 */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
