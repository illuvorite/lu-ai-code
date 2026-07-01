package com.lu.luaicode.service;


import com.lu.luaicode.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.lu.luaicode.model.dto.entity.ChatHistory;
import com.lu.luaicode.model.dto.entity.User;
import com.lu.luaicode.model.vo.ChatHistoryVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
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



    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
