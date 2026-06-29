package com.lu.luaicode.service;

import com.lu.luaicode.model.vo.LoginUserVO;
import com.lu.luaicode.model.dto.user.UserQueryRequest;
import com.lu.luaicode.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.lu.luaicode.model.dto.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author illusory
 */
public interface UserService extends IService<User> {


    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);
    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 用户登录（支持账号或邮箱登录）
     *
     * @param userAccount  用户账户
     * @param userEmail    用户邮箱
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userEmail, String userPassword, HttpServletRequest request);


    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param userEmail 用户邮箱
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount,String userEmail, String userPassword, String checkPassword);


/**
 * 根据用户查询请求参数构建查询包装器
 *
 * @param userQueryRequest 用户查询请求参数对象，包含查询条件
 * @return QueryWrapper 构建好的查询包装器，用于构建数据库查询条件
 */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);




    /**
 * 获取加密后的密码字符串
 * @param userPassword 用户原始密码字符串
 * @return 返回加密后的密码字符串
 */
    String getEncryptPassword(String userPassword);
}
