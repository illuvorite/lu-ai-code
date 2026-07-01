package com.lu.luaicode.controller;


import com.lu.luaicode.annotation.AuthCheck;
import com.lu.luaicode.common.Result;
import com.lu.luaicode.constant.UserConstant;
import com.lu.luaicode.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.lu.luaicode.model.dto.entity.ChatHistory;
import com.lu.luaicode.model.dto.entity.User;
import com.lu.luaicode.model.vo.ChatHistoryVO;
import com.lu.luaicode.service.ChatHistoryService;
import com.lu.luaicode.service.UserService;
import com.lu.luaicode.exception.ThrowUtils;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.lu.luaicode.exception.ResultCode.PARAM_ERROR;

/**
 * 对话历史 控制层。
 *
 * @author illusory
 */
@RestController
@RequestMapping("/chatHistory")
@Tag(name = "对话历史接口")
@Slf4j
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    @Operation(summary = "分页查询某个应用的对话历史（游标查询）")
    public Result<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.getChatHistoryPage(appId, pageSize, lastCreateTime, loginUser);
        return Result.success(result);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @Operation(summary = "管理员分页查询所有对话历史")
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN})
    public Result<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, PARAM_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return Result.success(result);
    }


}
