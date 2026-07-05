package com.lu.luaicode.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.lu.luaicode.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.lu.luaicode.model.dto.entity.App;
import com.lu.luaicode.model.dto.entity.ChatHistory;
import com.lu.luaicode.model.dto.entity.User;
import com.lu.luaicode.model.enums.MessageTypeEnum;
import com.lu.luaicode.model.enums.UserRoleEnum;
import com.lu.luaicode.mapper.ChatHistoryMapper;
import com.lu.luaicode.service.AppService;
import com.lu.luaicode.service.ChatHistoryService;
import com.lu.luaicode.exception.BusinessException;
import com.lu.luaicode.exception.ThrowUtils;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @Override
    public void exportChatHistoryToMarkdown(Long appId, HttpServletResponse response, User loginUser) {
        // 1. 校验应用存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, NOT_FOUND, "应用不存在");

        // 2. 权限校验：仅应用创建者或管理员可导出
        boolean isOwner = app.getUserId().equals(loginUser.getId());
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())
                || UserRoleEnum.SUPERADMIN.getValue().equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!isOwner && !isAdmin, NO_AUTH_ERROR, "无权导出该应用的对话历史");

        // 3. 设置响应头，触发浏览器文件下载
        String filename = "chat-history-" + appId + "-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".md";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("text/markdown; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);

        // 4. 流式分页写入 Markdown
        int pageSize = 200;
        int pageNum = 1;
        long total;

        try (PrintWriter writer = response.getWriter()) {
            // 写文件头
            writer.println("# 对话历史");
            writer.println();
            writer.println("- **应用**: " + app.getAppName());
            writer.println("- **导出时间**: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println();
            writer.println("---");
            writer.println();

            do {
                QueryWrapper queryWrapper = QueryWrapper.create()
                        .eq("appId", appId)
                        .orderBy("createTime", true); // 升序 = 从最早到最新

                Page<ChatHistory> chatHistoryPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
                List<ChatHistory> records = chatHistoryPage.getRecords();
                total = chatHistoryPage.getTotalRow();

                for (ChatHistory chatHistory : records) {
                    writeMessageAsMarkdown(writer, chatHistory);
                }

                writer.flush();
                pageNum++;
            } while ((long) (pageNum - 1) * pageSize < total);

            log.info("导出对话历史成功，appId={}, 共 {} 条记录", appId, total);
        } catch (IOException e) {
            log.error("导出对话历史失败，appId={}", appId, e);
            throw new BusinessException(INTERNAL_ERROR, "导出对话历史失败");
        }
    }

    /**
     * 将单条对话历史写入 Markdown 格式
     */
    private void writeMessageAsMarkdown(PrintWriter writer, ChatHistory chatHistory) {
        String timestamp = chatHistory.getCreateTime() != null
                ? chatHistory.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : "未知时间";

        String typeLabel = MessageTypeEnum.USER.getValue().equals(chatHistory.getMessageType())
                ? "👤 用户"
                : "🤖 AI";
        writer.println("## " + typeLabel + " — " + timestamp);
        writer.println();

        String message = chatHistory.getMessage();
        if (message != null) {
            // 使用 text 代码块包裹内容，防止内容中的 Markdown 语法破坏格式
            // 转义内容中的 triple backticks 以免提前闭合代码块
            String escaped = message.replace("```", "`\\`\\`");
            writer.println("```text");
            writer.println(escaped);
            writer.println("```");
        } else {
            writer.println("*(空消息)*");
        }

        writer.println();
        writer.println("---");
        writer.println();
    }


/**
 * 加载聊天历史到内存中的方法
 * @param appId 应用ID，用于标识特定应用
 * @param chatMemory 聊天内存对象，用于存储加载的聊天历史
 * @param maxCount 最大加载的聊天历史数量
 * @return 实际加载的聊天历史数量，如果加载失败则返回0
 */
    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
        // 创建查询条件，按应用ID筛选，按创建时间降序排列，并限制查询数量
            QueryWrapper queryWrapper = QueryWrapper.create()  // 创建查询条件构造器
                    .eq(ChatHistory::getAppId, appId)  // 设置应用ID等于传入的appId
                    .orderBy(ChatHistory::getCreateTime, false)  // 按创建时间降序排列
                    .limit(1, maxCount);  // 设置查询范围，从第1条开始，最多查询maxCount条
        // 执行查询，获取聊天历史列表
            List<ChatHistory> historyList = this.list(queryWrapper);  // 使用查询条件执行查询
        // 如果列表为空，直接返回0
            if (CollUtil.isEmpty(historyList)) {  // 检查查询结果是否为空
                return 0;
            }
        // 将查询结果反转，使最早的对话在前
            CollUtil.reverse(historyList);  // 反转列表，使最早的对话在前
            int loadedCount = 0;  // 记录成功加载的聊天历史数量
            chatMemory.clear();  // 清空聊天内存
        // 遍历聊天历史列表
            for (ChatHistory history : historyList) {  // 遍历每一条聊天历史
            // 判断消息类型，如果是用户消息，则添加用户消息到内存
                if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {  // 判断是否为用户消息
                    chatMemory.add(UserMessage.from(history.getMessage()));  // 添加用户消息到内存
                } else if (MessageTypeEnum.AI.getValue().equals(history.getMessageType())) {  // 判断是否为AI消息
                    chatMemory.add(AiMessage.from(history.getMessage()));  // 添加AI消息到内存
                }
                loadedCount++;  // 增加加载计数
            }
            log.info("成功为appId:{},加载了 {} 条对话历史",appId, loadedCount);  // 记录加载成功的日志
            return loadedCount;  // 返回成功加载的数量
        } catch (Exception e) {  // 捕获可能的异常
            log.error("加载对话历史失败", e);  // 记录错误日志
            return 0;  // 返回0表示加载失败
        }
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
