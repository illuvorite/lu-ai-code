# App Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement complete App CRUD module with user-owner and admin role-based access control.

**Architecture:** Follows existing User module pattern — Controller → Service/ServiceImpl (MyBatis-Flex) → Mapper. No tests as project has no test convention.

**Tech Stack:** Spring Boot, MyBatis-Flex, Hutool, Swagger/OpenAPI, Snowflake ID

---

### Task 1: Create DTO Request Classes

**Files:**
- Create: `src/main/java/com/lu/luaicode/model/dto/app/AppAddRequest.java`
- Create: `src/main/java/com/lu/luaicode/model/dto/app/AppUpdateRequest.java`
- Create: `src/main/java/com/lu/luaicode/model/dto/app/AppAdminUpdateRequest.java`
- Create: `src/main/java/com/lu/luaicode/model/dto/app/AppQueryRequest.java`

- [ ] **Create AppAddRequest.java**

```java
package com.lu.luaicode.model.dto.app;

import lombok.Data;
import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {
    private String initPrompt;
    private static final long serialVersionUID = 1L;
}
```

- [ ] **Create AppUpdateRequest.java**

```java
package com.lu.luaicode.model.dto.app;

import lombok.Data;
import java.io.Serializable;

@Data
public class AppUpdateRequest implements Serializable {
    private Long id;
    private String appName;
    private static final long serialVersionUID = 1L;
}
```

- [ ] **Create AppAdminUpdateRequest.java**

```java
package com.lu.luaicode.model.dto.app;

import lombok.Data;
import java.io.Serializable;

@Data
public class AppAdminUpdateRequest implements Serializable {
    private Long id;
    private String appName;
    private String cover;
    private Integer priority;
    private static final long serialVersionUID = 1L;
}
```

- [ ] **Create AppQueryRequest.java**

```java
package com.lu.luaicode.model.dto.app;

import com.lu.luaicode.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryRequest extends PageRequest implements Serializable {
    private Long id;
    private String appName;
    private Long userId;
    private String codeGenType;
    private static final long serialVersionUID = 1L;
}
```

---

### Task 2: Create AppVO View Object

**Files:**
- Create: `src/main/java/com/lu/luaicode/model/vo/AppVO.java`

- [ ] **Create AppVO.java**

```java
package com.lu.luaicode.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AppVO implements Serializable {
    private Long id;
    private String appName;
    private String cover;
    private String initPrompt;
    private String codeGenType;
    private Integer priority;
    private Long userId;
    private LocalDateTime createTime;
    private static final long serialVersionUID = 1L;
}
```

---

### Task 3: Implement Service Layer

**Files:**
- Modify: `src/main/java/com/lu/luaicode/service/AppService.java` (add methods)
- Modify: `src/main/java/com/lu/luaicode/service/impl/AppServiceImpl.java` (implement methods)

- [ ] **Update AppService interface**

```java
package com.lu.luaicode.service;

import com.lu.luaicode.model.dto.app.AppAddRequest;
import com.lu.luaicode.model.dto.app.AppAdminUpdateRequest;
import com.lu.luaicode.model.dto.app.AppUpdateRequest;
import com.lu.luaicode.model.dto.app.AppQueryRequest;
import com.lu.luaicode.model.vo.AppVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.lu.luaicode.model.dto.entity.App;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface AppService extends IService<App> {

    Long addApp(AppAddRequest appAddRequest, HttpServletRequest request);

    Boolean updateMyApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request);

    Boolean deleteMyApp(Long id, HttpServletRequest request);

    App getMyAppById(Long id, HttpServletRequest request);

    Page<AppVO> listMyApps(AppQueryRequest appQueryRequest, HttpServletRequest request);

    Page<AppVO> listFeaturedApps(AppQueryRequest appQueryRequest);

    Boolean adminDeleteApp(Long id);

    Boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest);

    Page<App> adminListApps(AppQueryRequest appQueryRequest);

    App adminGetAppById(Long id);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    AppVO getAppVO(App app);

    List<AppVO> getAppVOList(List<App> appList);
}
```

- [ ] **Implement AppServiceImpl**

```java
package com.lu.luaicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.lu.luaicode.model.dto.app.AppAddRequest;
import com.lu.luaicode.model.dto.app.AppAdminUpdateRequest;
import com.lu.luaicode.model.dto.app.AppUpdateRequest;
import com.lu.luaicode.model.dto.app.AppQueryRequest;
import com.lu.luaicode.model.dto.entity.User;
import com.lu.luaicode.model.vo.AppVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lu.luaicode.model.dto.entity.App;
import com.lu.luaicode.mapper.AppMapper;
import com.lu.luaicode.service.AppService;
import com.lu.luaicode.service.UserService;
import com.lu.luaicode.exception.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.lu.luaicode.exception.ResultCode.*;

@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Override
    public Long addApp(AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(StrUtil.isBlank(appAddRequest.getInitPrompt()), PARAM_ERROR, "initPrompt 不能为空");
        User loginUser = userService.getLoginUser(request);
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        boolean save = this.save(app);
        ThrowUtils.throwIf(!save, DATA_OPERATION_FAIL, "创建应用失败");
        return app.getId();
    }

    @Override
    public Boolean updateMyApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        Long id = appUpdateRequest.getId();
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, NOT_FOUND, "应用不存在");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!oldApp.getUserId().equals(loginUser.getId()), NO_AUTH_ERROR, "无权修改他人应用");
        App app = new App();
        app.setId(id);
        app.setAppName(appUpdateRequest.getAppName());
        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, DATA_OPERATION_FAIL, "更新应用失败");
        return true;
    }

    @Override
    public Boolean deleteMyApp(Long id, HttpServletRequest request) {
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, NOT_FOUND, "应用不存在");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!oldApp.getUserId().equals(loginUser.getId()), NO_AUTH_ERROR, "无权删除他人应用");
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, DATA_OPERATION_FAIL, "删除应用失败");
        return true;
    }

    @Override
    public App getMyAppById(Long id, HttpServletRequest request) {
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, NOT_FOUND, "应用不存在");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), NO_AUTH_ERROR, "无权查看他人应用");
        return app;
    }

    @Override
    public Page<AppVO> listMyApps(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = Math.min(appQueryRequest.getPageSize(), 20);
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        queryWrapper.eq("userId", loginUser.getId());
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        appVOPage.setRecords(this.getAppVOList(appPage.getRecords()));
        return appVOPage;
    }

    @Override
    public Page<AppVO> listFeaturedApps(AppQueryRequest appQueryRequest) {
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = Math.min(appQueryRequest.getPageSize(), 20);
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (StrUtil.isNotBlank(appQueryRequest.getAppName())) {
            queryWrapper.like("appName", appQueryRequest.getAppName());
        }
        queryWrapper.orderBy("priority", false).orderBy("createTime", false);
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        appVOPage.setRecords(this.getAppVOList(appPage.getRecords()));
        return appVOPage;
    }

    @Override
    public Boolean adminDeleteApp(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, PARAM_ERROR);
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, DATA_OPERATION_FAIL, "删除应用失败");
        return true;
    }

    @Override
    public Boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null, PARAM_ERROR);
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, DATA_OPERATION_FAIL, "更新应用失败");
        return true;
    }

    @Override
    public Page<App> adminListApps(AppQueryRequest appQueryRequest) {
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        return this.page(Page.of(pageNum, pageSize), queryWrapper);
    }

    @Override
    public App adminGetAppById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, PARAM_ERROR);
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, NOT_FOUND, "应用不存在");
        return app;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, PARAM_ERROR, "请求参数为空");
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        Long userId = appQueryRequest.getUserId();
        String codeGenType = appQueryRequest.getCodeGenType();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userId", userId)
                .eq("codeGenType", codeGenType)
                .like("appName", appName)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) return null;
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) return new ArrayList<>();
        return appList.stream().map(this::getAppVO).collect(Collectors.toList());
    }
}
```

---

### Task 4: Implement Controller Layer

**Files:**
- Modify: `src/main/java/com/lu/luaicode/controller/AppController.java`

- [ ] **Update AppController**

```java
package com.lu.luaicode.controller;

import com.lu.luaicode.annotation.AuthCheck;
import com.lu.luaicode.common.DeleteRequest;
import com.lu.luaicode.common.Result;
import com.lu.luaicode.constant.UserConstant;
import com.lu.luaicode.model.dto.app.AppAddRequest;
import com.lu.luaicode.model.dto.app.AppAdminUpdateRequest;
import com.lu.luaicode.model.dto.app.AppUpdateRequest;
import com.lu.luaicode.model.dto.app.AppQueryRequest;
import com.lu.luaicode.model.vo.AppVO;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import com.lu.luaicode.model.dto.entity.App;
import com.lu.luaicode.service.AppService;
import com.lu.luaicode.exception.ThrowUtils;
import org.springframework.web.bind.annotation.*;
import static com.lu.luaicode.exception.ResultCode.PARAM_ERROR;

@RestController
@RequestMapping("/app")
@Tag(name = "应用接口")
public class AppController {

    @Resource
    private AppService appService;

    @PostMapping("/add")
    @Operation(summary = "创建应用")
    public Result<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, PARAM_ERROR);
        Long appId = appService.addApp(appAddRequest, request);
        return Result.success(appId);
    }

    @PostMapping("/update")
    @Operation(summary = "修改自己的应用")
    public Result<Boolean> updateMyApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, PARAM_ERROR);
        Boolean result = appService.updateMyApp(appUpdateRequest, request);
        return Result.success(result);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除自己的应用")
    public Result<Boolean> deleteMyApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0, PARAM_ERROR);
        Boolean result = appService.deleteMyApp(deleteRequest.getId(), request);
        return Result.success(result);
    }

    @GetMapping("/get")
    @Operation(summary = "查看自己的应用详情")
    public Result<App> getMyAppById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, PARAM_ERROR);
        App app = appService.getMyAppById(id, request);
        return Result.success(app);
    }

    @PostMapping("/list/my")
    @Operation(summary = "分页查询自己的应用列表")
    public Result<Page<AppVO>> listMyApps(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, PARAM_ERROR);
        Page<AppVO> page = appService.listMyApps(appQueryRequest, request);
        return Result.success(page);
    }

    @PostMapping("/list/featured")
    @Operation(summary = "分页查询精选应用列表")
    public Result<Page<AppVO>> listFeaturedApps(@RequestBody(required = false) AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            appQueryRequest = new AppQueryRequest();
        }
        Page<AppVO> page = appService.listFeaturedApps(appQueryRequest);
        return Result.success(page);
    }

    @PostMapping("/delete/admin")
    @Operation(summary = "管理员删除应用")
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN})
    public Result<Boolean> adminDeleteApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0, PARAM_ERROR);
        Boolean result = appService.adminDeleteApp(deleteRequest.getId());
        return Result.success(result);
    }

    @PostMapping("/update/admin")
    @Operation(summary = "管理员更新应用")
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN})
    public Result<Boolean> adminUpdateApp(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null, PARAM_ERROR);
        Boolean result = appService.adminUpdateApp(appAdminUpdateRequest);
        return Result.success(result);
    }

    @PostMapping("/list/admin")
    @Operation(summary = "管理员分页查询应用列表")
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN})
    public Result<Page<App>> adminListApps(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, PARAM_ERROR);
        Page<App> page = appService.adminListApps(appQueryRequest);
        return Result.success(page);
    }

    @GetMapping("/get/admin")
    @Operation(summary = "管理员查看应用详情")
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN})
    public Result<App> adminGetAppById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, PARAM_ERROR);
        App app = appService.adminGetAppById(id);
        return Result.success(app);
    }
}
```
