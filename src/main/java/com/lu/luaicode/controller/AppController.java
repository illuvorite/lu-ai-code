package com.lu.luaicode.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.lu.luaicode.annotation.AuthCheck;
import com.lu.luaicode.common.DeleteRequest;
import com.lu.luaicode.common.Result;
import com.lu.luaicode.constant.UserConstant;
import com.lu.luaicode.model.dto.app.*;
import com.lu.luaicode.model.dto.entity.User;
import com.lu.luaicode.model.vo.AppVO;
import com.lu.luaicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import com.lu.luaicode.model.dto.entity.App;
import com.lu.luaicode.service.AppService;
import com.lu.luaicode.exception.ThrowUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.lu.luaicode.exception.ResultCode.NOT_FOUND;
import static com.lu.luaicode.exception.ResultCode.PARAM_ERROR;

/**
 * 应用 控制层。
 *
 * @author illusory
 */
@RestController
@RequestMapping("/app")
@Tag(name = "应用接口")
@Slf4j
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 应用聊天生成代码（流式 SSE）
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @param request 请求对象
     * @return 生成结果流
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "应用聊天生成代码（流式 SSE）")
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       HttpServletRequest request) {
        try {
            // 参数校验
            ThrowUtils.throwIf(appId == null || appId <= 0, PARAM_ERROR, "应用ID无效");
            ThrowUtils.throwIf(StrUtil.isBlank(message), PARAM_ERROR, "用户消息不能为空");
            // 获取当前登录用户
            User loginUser = userService.getLoginUser(request);
            // 调用服务生成代码（流式）
            Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
            // 转换为 ServerSentEvent 格式
            return contentFlux
                    .map(chunk -> {
                        // 将内容包装成JSON对象
                        Map<String, String> wrapper = Map.of("d", chunk);
                        String jsonData = JSONUtil.toJsonStr(wrapper);
                        return ServerSentEvent.<String>builder()
                                .data(jsonData)
                                .build();
                    })
                    .concatWith(Mono.just(
                            // 发送结束事件
                            ServerSentEvent.<String>builder()
                                    .event("done")
                                    .data("")
                                    .build()
                    ))
                    .onErrorResume(e -> {
                        // 捕获流式处理异常（如 LangChain4j 连接断开），发送错误事件而非崩溃
                        log.error("SSE 流式生成代码异常: {}", e.getMessage());
                        return Mono.just(ServerSentEvent.<String>builder()
                                .event("business-error")
                                .data("{\"message\": \"AI 服务连接异常，请重试\"}")
                                .build());
                    });
        } catch (Exception e) {
            // 捕获参数校验或用户认证阶段的异常，同样以 SSE 错误事件返回
            log.error("SSE 请求预处理异常: {}", e.getMessage());
            String errorJson = JSONUtil.toJsonStr(Map.of("message", e.getMessage()));
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("business-error")
                    .data(errorJson)
                    .build());
        }
    }



    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    @Operation(summary = "应用部署")
    public Result<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, PARAM_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, PARAM_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return Result.success(deployUrl);
    }





    // ==================== 用户端接口 ====================

    /**
     * 创建应用
     *
     * @param appAddRequest 创建应用请求（initPrompt 必填）
     * @param request       HTTP 请求
     * @return 新创建的应用 id
     */
    @PostMapping("/add")
    @Operation(summary = "创建应用")
    public Result<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, PARAM_ERROR);
        Long appId = appService.addApp(appAddRequest, request);
        return Result.success(appId);
    }

    /**
     * 修改自己的应用（只允许修改应用名称）
     *
     * @param appUpdateRequest 修改应用请求
     * @param request          HTTP 请求
     * @return 是否修改成功
     */
    @PostMapping("/update")
    @Operation(summary = "修改自己的应用")
    public Result<Boolean> updateMyApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, PARAM_ERROR);
        Boolean result = appService.updateMyApp(appUpdateRequest, request);
        return Result.success(result);
    }

    /**
     * 删除自己的应用
     *
     * @param deleteRequest 删除请求（包含应用 id）
     * @param request       HTTP 请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @Operation(summary = "删除自己的应用")
    public Result<Boolean> deleteMyApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0, PARAM_ERROR);
        Boolean result = appService.deleteMyApp(deleteRequest.getId(), request);
        return Result.success(result);
    }


    /**
     * 根据 id 获取应用详情
     *
     * @param id      应用 id
     * @return 应用详情
     */
    @GetMapping("/get/vo")
    @Operation(summary = "根据 id 获取应用详情")
    public Result<AppVO> getAppVOById(long id) {
        ThrowUtils.throwIf(id <= 0, PARAM_ERROR);
        // 查询数据库
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, NOT_FOUND);
        // 获取封装类（包含用户信息）
        return Result.success(appService.getAppVO(app));
    }

    /**
     * 查看自己的应用详情
     *
     * @param id      应用 id
     * @param request HTTP 请求
     * @return 应用详情
     */
    @GetMapping("/get")
    @Operation(summary = "查看自己的应用详情")
    public Result<App> getMyAppById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, PARAM_ERROR);
        App app = appService.getMyAppById(id, request);
        return Result.success(app);
    }

    /**
     * 分页查询自己的应用列表（支持根据名称查询，每页最多 20 条）
     *
     * @param appQueryRequest 分页查询请求
     * @param request         HTTP 请求
     * @return 分页结果
     */
    @PostMapping("/list/my/page")
    @Operation(summary = "分页查询自己的应用列表")
    public Result<Page<AppVO>> listMyApps(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, PARAM_ERROR);
        Page<AppVO> page = appService.listMyApps(appQueryRequest, request);
        return Result.success(page);
    }

    /**
     * 分页查询精选应用列表（按 priority 降序排列，每页最多 20 条）
     * 无需登录即可访问
     *
     * @param appQueryRequest 分页查询请求（可选，为空时返回默认分页）
     * @return 分页结果
     */
    @PostMapping("/list/featured/page")
    @Operation(summary = "分页查询精选应用列表")
    public Result<Page<AppVO>> listFeaturedApps(@RequestBody(required = false) AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, PARAM_ERROR);
        Page<AppVO> page = appService.listFeaturedApps(appQueryRequest);
        return Result.success(page);
    }

    // ==================== 管理员端接口 ====================

    /**
     * 管理员删除任意应用
     *
     * @param deleteRequest 删除请求（包含应用 id）
     * @return 是否删除成功
     */
    @PostMapping("/admin/delete")
    @Operation(summary = "管理员删除应用")
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN})
    public Result<Boolean> adminDeleteApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0, PARAM_ERROR);
        Long id = deleteRequest.getId();
        boolean result = appService.adminDeleteApp(id);
        return Result.success(result);
    }

    /**
     * 管理员更新任意应用（支持修改应用名称、封面、优先级）
     *
     * @param appAdminUpdateRequest 管理员更新请求
     * @return 是否更新成功
     */
    @PostMapping("/admin/update")
    @Operation(summary = "管理员更新应用")
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN})
    public Result<Boolean> adminUpdateApp(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null, PARAM_ERROR);
        Boolean result = appService.adminUpdateApp(appAdminUpdateRequest);
        return Result.success(result);
    }

    /**
     * 管理员分页查询应用列表（支持按任意字段查询，每页数量不限）
     *
     * @param appQueryRequest 分页查询请求
     * @return 分页结果
     */
    @PostMapping("/admin/list/page")
    @Operation(summary = "管理员分页查询应用列表")
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN})
    public Result<Page<AppVO>> adminListApps(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, PARAM_ERROR);
        Page<AppVO> appVOPage = appService.adminListApps(appQueryRequest);
        return Result.success(appVOPage);
    }

    /**
     * 管理员查看任意应用详情
     *
     * @param id 应用 id
     * @return 应用详情
     */
    @GetMapping("/admin/get")
    @Operation(summary = "管理员查看应用详情")
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN})
    public Result<App> adminGetAppById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, PARAM_ERROR);
        App app = appService.adminGetAppById(id);
        return Result.success(app);
    }
}
