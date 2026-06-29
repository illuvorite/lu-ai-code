package com.lu.luaicode.controller;

import cn.hutool.core.bean.BeanUtil;
import com.lu.luaicode.annotation.AuthCheck;
import com.lu.luaicode.common.DeleteRequest;
import com.lu.luaicode.common.Result;
import com.lu.luaicode.constant.UserConstant;
import com.lu.luaicode.model.dto.user.*;
import com.lu.luaicode.model.vo.LoginUserVO;
import com.lu.luaicode.exception.ResultCode;
import com.lu.luaicode.exception.ThrowUtils;
import com.lu.luaicode.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.lu.luaicode.model.dto.entity.User;
import com.lu.luaicode.service.UserService;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import static com.lu.luaicode.exception.ResultCode.PARAM_ERROR;

/**
 * 用户 控制层。
 *
 * @author illusory
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户接口")
public class UserController {

    @Resource
    private UserService userService;


    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<LoginUserVO> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, PARAM_ERROR);
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest.getUserAccount(), userLoginRequest.getUserEmail(), userLoginRequest.getUserPassword(), request);
        return Result.success(loginUserVO);
    }


    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, PARAM_ERROR, "用户信息不能为空");
        long result = userService.userRegister(userRegisterRequest.getUserAccount(),
                userRegisterRequest.getUserEmail(),
                userRegisterRequest.getUserPassword(),
                userRegisterRequest.getCheckPassword());
        return Result.success(result);
    }
    @PostMapping("/logout")
    @Operation(summary = "登出")
    public Result<Boolean> logout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, PARAM_ERROR);
        return Result.success(userService.userLogout(request));
    }

    @GetMapping("/get/login")
    @Operation(summary = "获取当前登录用户")
    public Result<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return Result.success(userService.getLoginUserVO(loginUser));
    }

    @PostMapping("/add")
    @Operation(summary = "新增用户")
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE,UserConstant.SUPER_ADMIN})
    public Result<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, PARAM_ERROR, "用户信息不能为空");
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        final String DEFAULT_PASSWORD="12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save, PARAM_ERROR, "新增用户失败");
        return Result.success(user.getId());
    }


    /**
     * 根据id查询用户
     */
    @GetMapping("/get")
    @Operation(summary = "根据id查询用户")
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE,UserConstant.SUPER_ADMIN})
    public Result<User> getUserById(Long id) {
        ThrowUtils.throwIf(id <=0, PARAM_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user==null, ResultCode.NOT_FOUND);
        return Result.success(user);
    }


    /**
     * 根据id获取封装类

 * 该接口通过传入用户ID，获取对应的用户信息并将其转换为视图对象(UserVO)返回
     */
    @GetMapping("/get/vo")
    @Operation(summary = "获取用户列表")
    public Result<UserVO> listUserPage(Long id) {
    // 调用getUserById方法根据id获取用户信息
        Result<User> userById = getUserById(id);
    // 通过userService的getUserVO方法将用户对象转换为视图对象
        UserVO userVO = userService.getUserVO(userById.getData());
    // 返回成功响应，包含转换后的UserVO对象
        return Result.success(userVO);
    }


    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @Operation(summary = "删除用户")
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE,UserConstant.SUPER_ADMIN})
    public Result<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null||deleteRequest.getId()<=0, PARAM_ERROR);
        boolean result = userService.removeById(deleteRequest.getId());
        return Result.success(result);
    }

    @PostMapping("/update")
    @Operation(summary = "更新用户")
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE,UserConstant.SUPER_ADMIN})
    public Result<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null||userUpdateRequest.getId()==null, PARAM_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ResultCode.DATA_OPERATION_FAIL, "更新用户失败");
        return Result.success(true);
    }

    /**
     * 分页获取用户封装列表(仅管理员)
 * 该接口用于管理员分页查询用户信息，并返回封装后的视图对象(UserVO)
 * 需要管理员权限才能访问
     */
    @PostMapping("/list/page/vo")
    @Operation(summary = "分页获取用户封装列表")  // 接口描述，用于API文档生成
    @AuthCheck(mustRole = {UserConstant.ADMIN_ROLE,UserConstant.SUPER_ADMIN})  // 权限校验，仅管理员可访问
    public Result<Page<UserVO>> listUserPage(@RequestBody UserQueryRequest userQueryRequest) {
    // 参数校验：如果请求参数为空，则抛出参数错误异常
        ThrowUtils.throwIf(userQueryRequest == null, PARAM_ERROR, "请求参数为空");

    // 获取分页参数
        int pageNum = userQueryRequest.getPageNum();    // 当前页码
        int pageSize = userQueryRequest.getPageSize();    // 每页大小
    // 查询用户分页数据
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize), userService.getQueryWrapper(userQueryRequest));

    //数据脱敏
    // 创建用户视图对象分页信息
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());  // 设置分页信息
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());  // 转换为视图对象列表
        userVOPage.setRecords(userVOList);  // 设置分页数据
    // 返回成功结果
        return Result.success(userVOPage);
    }

}
