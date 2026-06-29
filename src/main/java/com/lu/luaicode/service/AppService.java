package com.lu.luaicode.service;

import com.lu.luaicode.model.dto.app.AppAddRequest;
import com.lu.luaicode.model.dto.app.AppAdminUpdateRequest;
import com.lu.luaicode.model.dto.app.AppUpdateRequest;
import com.lu.luaicode.model.dto.app.AppQueryRequest;
import com.lu.luaicode.model.dto.entity.User;
import com.lu.luaicode.model.vo.AppVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.lu.luaicode.model.dto.entity.App;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author illusory
 */
public interface AppService extends IService<App> {

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    String deployApp(Long appId, User loginUser);

    /**
     * 创建应用
     *
     * @param appAddRequest 创建应用请求（initPrompt 必填）
     * @param request       HTTP 请求
     * @return 新创建的应用 id
     */
    Long addApp(AppAddRequest appAddRequest, HttpServletRequest request);

    /**
     * 修改自己的应用（只允许修改应用名称）
     *
     * @param appUpdateRequest 修改应用请求
     * @param request          HTTP 请求
     * @return 是否修改成功
     */
    Boolean updateMyApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request);

    /**
     * 删除自己的应用
     *
     * @param id      应用 id
     * @param request HTTP 请求
     * @return 是否删除成功
     */
    Boolean deleteMyApp(Long id, HttpServletRequest request);

    /**
     * 查看自己的应用详情
     *
     * @param id      应用 id
     * @param request HTTP 请求
     * @return 应用详情
     */
    App getMyAppById(Long id, HttpServletRequest request);

    /**
     * 分页查询自己的应用列表（每页最多 20 条）
     *
     * @param appQueryRequest 分页查询请求
     * @param request         HTTP 请求
     * @return 分页结果
     */
    Page<AppVO> listMyApps(AppQueryRequest appQueryRequest, HttpServletRequest request);

    /**
     * 分页查询精选应用列表（按 priority 降序，每页最多 20 条）
     *
     * @param appQueryRequest 分页查询请求
     * @return 分页结果
     */
    Page<AppVO> listFeaturedApps(AppQueryRequest appQueryRequest);


    /**
     * 管理员更新任意应用（支持修改应用名称、封面、优先级）
     *
     * @param appAdminUpdateRequest 管理员更新请求
     * @return 是否更新成功
     */
    Boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest);

    /**
     * 管理员分页查询应用列表（每页数量不限）
     *
     * @param appQueryRequest 分页查询请求
     * @return 分页结果
     */
    Page<AppVO> adminListApps(AppQueryRequest appQueryRequest);

    /**
     * 管理员查看任意应用详情
     *
     * @param id 应用 id
     * @return 应用详情
     */
    App adminGetAppById(Long id);

    /**
     * 构建查询包装器
     *
     * @param appQueryRequest 查询请求
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用视图对象
     *
     * @param app 应用实体
     * @return 应用视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用视图对象列表
     *
     * @param appList 应用实体列表
     * @return 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);
}
