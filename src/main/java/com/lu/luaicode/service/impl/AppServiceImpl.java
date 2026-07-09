package com.lu.luaicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.lu.luaicode.constant.AppConstant;
import com.lu.luaicode.core.AiCodeGeneratorFacade;
import com.lu.luaicode.core.builder.VueProjectBuilder;
import com.lu.luaicode.core.handler.StreamHandlerExecutor;
import com.lu.luaicode.exception.BusinessException;
import com.lu.luaicode.model.dto.app.AppAddRequest;
import com.lu.luaicode.model.dto.app.AppAdminUpdateRequest;
import com.lu.luaicode.model.dto.app.AppUpdateRequest;
import com.lu.luaicode.model.dto.app.AppQueryRequest;
import com.lu.luaicode.model.dto.entity.User;
import com.lu.luaicode.model.enums.CodeGenTypeEnum;
import com.lu.luaicode.model.enums.MessageTypeEnum;
import com.lu.luaicode.model.enums.UserRoleEnum;
import com.lu.luaicode.model.vo.AppVO;
import com.lu.luaicode.model.vo.UserVO;
import com.lu.luaicode.mq.ScreenshotTaskProducer;
import com.lu.luaicode.service.ScreenshotService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lu.luaicode.model.dto.entity.App;
import com.lu.luaicode.mapper.AppMapper;
import com.lu.luaicode.service.AppService;
import com.lu.luaicode.service.ChatHistoryService;
import com.lu.luaicode.service.UserService;
import com.lu.luaicode.exception.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lu.luaicode.exception.ResultCode.*;

/**
 * 应用 服务层实现。
 *
 * @author illusory
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;
    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private ScreenshotTaskProducer screenshotTaskProducer;

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, PARAM_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), PARAM_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, NOT_FOUND, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(INTERNAL_ERROR, "不支持的代码生成类型");
        }
        // 5. 保存用户消息
        chatHistoryService.saveMessage(appId, loginUser.getId(), message, MessageTypeEnum.USER.getValue());

        // 6. 调用 AI 生成代码
        Flux<String> codeFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        // 7. 处理流式响应并保存 AI 消息
        return streamHandlerExecutor.doExecute(codeFlux, chatHistoryService, appId, loginUser, codeGenTypeEnum);
    }


    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, PARAM_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, NOT_FOUND, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        log.info("检查的生成代码目录: {}", sourceDirPath);
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(INTERNAL_ERROR, "应用代码不存在，请先生成代码");
        }
// 7. Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, INTERNAL_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), INTERNAL_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 将 dist 目录作为部署源
            sourceDir = distDir;
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", distDir.getAbsolutePath());
        }
        // 8. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            log.error("复制文件到部署目录失败: {}", e.getMessage(), e);
            throw new BusinessException(INTERNAL_ERROR, "复制文件到部署目录失败" + e.getMessage());
        }
        //9. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, DATA_OPERATION_FAIL, "更新应用部署信息失败");
        // 10. 返回可访问的 URL
        String appDeployUrl = String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        // 11. 异步生成截图并且更新应用封面
        generateAppScreenshotAsync(appId, appDeployUrl);
        return appDeployUrl;
    }


    /**
     * 异步生成应用截图并更新应用封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问url
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        screenshotTaskProducer.sendTask(appId, appUrl);//消息生产者 发送消息
        log.info("截图任务已投递: appId={}, url={}", appId, appUrl);
    }


    @Override
    public Long addApp(AppAddRequest appAddRequest, HttpServletRequest request) {
        // 校验 initPrompt 不允许为空
        ThrowUtils.throwIf(StrUtil.isBlank(appAddRequest.getInitPrompt()), PARAM_ERROR, "initPrompt 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 拷贝属性并设置所属用户
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        //应用名称暂时为initPrompt前12位
        app.setAppName(appAddRequest.getInitPrompt().substring(0, Math.min(appAddRequest.getInitPrompt().length(), 12)));
        app.setUserId(loginUser.getId());
        //暂时设置多文件生成
        app.setCodeGenType(CodeGenTypeEnum.VUE_PROJECT.getValue());
        boolean save = this.save(app);
        ThrowUtils.throwIf(!save, DATA_OPERATION_FAIL, "创建应用失败");
        return app.getId();
    }

    @Override
    public Boolean updateMyApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        // 校验应用是否存在
        Long id = appUpdateRequest.getId();
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, NOT_FOUND, "应用不存在");
        // 校验是否为应用所有者
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!oldApp.getUserId().equals(loginUser.getId()), NO_AUTH_ERROR, "无权修改他人应用");
        // 只允许修改应用名称
        App app = new App();
        app.setId(id);
        app.setAppName(appUpdateRequest.getAppName());
        app.setEditTime(LocalDateTime.now());
        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, DATA_OPERATION_FAIL, "更新应用失败");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteMyApp(Long id, HttpServletRequest request) {
        // 校验应用是否存在
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, NOT_FOUND, "应用不存在");
        // 校验是否为应用所有者或管理员
        User loginUser = userService.getLoginUser(request);
        boolean isOwner = oldApp.getUserId().equals(loginUser.getId());
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())
                || UserRoleEnum.SUPERADMIN.getValue().equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!isOwner && !isAdmin, NO_AUTH_ERROR, "无权删除他人应用");
        // 级联删除对话历史
        chatHistoryService.deleteByAppId(id);
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, DATA_OPERATION_FAIL, "删除应用失败");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminDeleteApp(Long id) {
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, NOT_FOUND, "应用不存在");
        // 级联删除对话历史
        chatHistoryService.deleteByAppId(id);
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, DATA_OPERATION_FAIL, "删除应用失败");
        return true;
    }

    @Override
    public App getMyAppById(Long id, HttpServletRequest request) {
        // 校验应用是否存在
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, NOT_FOUND, "应用不存在");
        // 校验是否为应用所有者
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), NO_AUTH_ERROR, "无权查看他人应用");
        return app;
    }

    @Override
    public Page<AppVO> listMyApps(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        // 获取当前登录用户，只查询该用户的应用
        User loginUser = userService.getLoginUser(request);
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, PARAM_ERROR, "每页最多 20 条");
        // 构建查询条件，根据用户 id 过滤
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        queryWrapper.eq("userId", loginUser.getId());
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据脱敏并返回
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        appVOPage.setRecords(this.getAppVOList(appPage.getRecords()));
        return appVOPage;
    }

    @Override
    public Page<AppVO> listFeaturedApps(AppQueryRequest appQueryRequest) {
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, PARAM_ERROR, "每页最多 20 条");
        // 构建查询条件，按 priority 降序排列
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        //分页查询
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据脱敏并返回
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        appVOPage.setRecords(this.getAppVOList(appPage.getRecords()));
        return appVOPage;
    }


    @Override
    public Boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null, PARAM_ERROR);
        Long id = appAdminUpdateRequest.getId();
        App oldApp = getById(id);
        ThrowUtils.throwIf(oldApp == null, NOT_FOUND);
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        app.setEditTime(LocalDateTime.now());
        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, DATA_OPERATION_FAIL, "更新应用失败");
        return true;
    }

    @Override
    public Page<AppVO> adminListApps(AppQueryRequest appQueryRequest) {
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        Page<App> appPage = page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        appVOPage.setRecords(this.getAppVOList(appPage.getRecords()));
        return appVOPage;
    }

    @Override
    public App adminGetAppById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, PARAM_ERROR);
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, NOT_FOUND, "应用不存在");
        return app;
    }

    /**
     * 构建查询包装器（根据请求参数动态拼接查询条件）
     *
     * @param appQueryRequest 查询请求
     * @return QueryWrapper
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, PARAM_ERROR, "请求参数为空");
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }


    @Override
    /**
     * 将App对象转换为AppVO对象
     * @param app 需要转换的App对象
     * @return 转换后的AppVO对象，如果输入为null则返回null
     */
    public AppVO getAppVO(App app) {
        // 如果输入的app对象为null，直接返回null
        if (app == null) {
            return null;
        }
        // 创建AppVO对象并复制app的属性
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 获取app关联的userId
        Long userId = app.getUserId();
        // 如果userId不为null，则查询对应的用户信息并设置到appVO中
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    /**
     * 将应用列表转换为应用视图对象列表
     * @param appList 应用列表
     * @return 应用视图对象列表
     */
    public List<AppVO> getAppVOList(List<App> appList) {
        // 如果应用列表为空，则返回空列表
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)  // 从应用对象中提取用户ID
                .collect(Collectors.toSet());  // 将用户ID收集为Set集合
        // 根据用户ID批量查询用户，并转换为用户视图对象映射表
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        // 遍历应用列表，将每个应用转换为视图对象并设置对应的用户信息
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);  // 获取应用视图对象
            UserVO userVO = userVOMap.get(app.getUserId());  // 从映射表中获取用户视图对象
            appVO.setUser(userVO);  // 设置用户信息到应用视图对象
            return appVO;
        }).collect(Collectors.toList());  // 将结果收集为列表
    }

}
