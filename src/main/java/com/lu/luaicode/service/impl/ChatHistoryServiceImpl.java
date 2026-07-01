package com.lu.luaicode.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.lu.luaicode.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.lu.luaicode.model.dto.entity.App;
import com.lu.luaicode.model.dto.entity.ChatHistory;
import com.lu.luaicode.model.dto.entity.User;
import com.lu.luaicode.model.enums.MessageTypeEnum;
import com.lu.luaicode.model.enums.UserRoleEnum;
import com.lu.luaicode.model.vo.ChatHistoryVO;
import com.lu.luaicode.mapper.ChatHistoryMapper;
import com.lu.luaicode.service.AppService;
import com.lu.luaicode.service.ChatHistoryService;
import com.lu.luaicode.exception.ThrowUtils;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.lu.luaicode.exception.ResultCode.*;

/**
 * 对话历史 服务层实现。
 *
 * @author illusory
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;

/**
 * 保存消息方法
 * @param appId 应用ID
 * @param userId 用户ID
 * @param message 消息内容
 * @param messageType 消息类型
 * @return 保存结果，成功返回true，失败返回false
 */
    @Override
    public boolean saveMessage(Long appId, Long userId, String message, String messageType) {
    // 校验应用ID是否有效
        ThrowUtils.throwIf(appId == null || appId <= 0, PARAM_ERROR, "应用 ID 不能为空");
    // 校验用户ID是否有效
        ThrowUtils.throwIf(userId == null || userId <= 0, PARAM_ERROR, "用户 ID 不能为空");
    // 校验消息内容是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(message), PARAM_ERROR, "消息不能为空");
    // 校验消息类型是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), PARAM_ERROR, "消息类型不能为空");

        //验证消息类型是否有效
        MessageTypeEnum enumByValue = MessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(enumByValue == null, PARAM_ERROR, "消息类型无效");
        //保存消息
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .message(message)
                .messageType(messageType)
                .createTime(LocalDateTime.now())
                .build();
        return this.save(chatHistory);
    }

    @Override
    public Page<ChatHistory> getChatHistoryPage(Long appId,int pageSize,LocalDateTime lastCreateTime, User loginUser) {
        ThrowUtils.throwIf(pageSize<=0||pageSize>50, PARAM_ERROR, "每页数量必须在1到50之间");
        ThrowUtils.throwIf(appId == null || appId <= 0, PARAM_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, NOT_LOGIN_ERROR);
        // 校验应用是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, NOT_FOUND, "应用不存在");

        // 权限校验：仅应用创建者或管理员可查看
        boolean isOwner = app.getUserId().equals(loginUser.getId());
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())
                || UserRoleEnum.SUPERADMIN.getValue().equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!isOwner && !isAdmin, NO_AUTH_ERROR, "无权查看该应用的对话历史");

        ChatHistoryQueryRequest queryRequest=new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);

        QueryWrapper queryWrapper =this.getQueryWrapper(queryRequest);

        return this.page(Page.of(1, pageSize), queryWrapper);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, PARAM_ERROR, "应用 ID 不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create().eq("appId", appId);
        this.remove(queryWrapper);
    }






    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(ChatHistory::getId, id)
                .like(ChatHistory::getMessage, message)
                .eq(ChatHistory::getMessageType, messageType)
                .eq(ChatHistory::getAppId, appId)
                .eq(ChatHistory::getUserId, userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt(ChatHistory::getCreateTime, lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy(ChatHistory::getCreateTime, false);
        }
        return queryWrapper;
    }

}
