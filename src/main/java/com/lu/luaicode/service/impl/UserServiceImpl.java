package com.lu.luaicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.lu.luaicode.model.vo.LoginUserVO;
import com.lu.luaicode.exception.BusinessException;
import com.lu.luaicode.exception.ThrowUtils;
import com.lu.luaicode.model.dto.user.UserQueryRequest;
import com.lu.luaicode.model.enums.UserRoleEnum;
import com.lu.luaicode.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lu.luaicode.domain.entity.User;
import com.lu.luaicode.mapper.UserMapper;
import com.lu.luaicode.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.lu.luaicode.constant.UserConstant.USER_LOGIN_STATE;
import static com.lu.luaicode.exception.ResultCode.*;

/**
 * 用户 服务层实现。
 *
 * @author illusory
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

    /**
     * 获取登录用户信息
     * @param user 用户对象
     * @return 登录用户信息对象
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user==null){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user,loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取当前登录用户
     * @param request HTTP请求对象
     * @return 当前登录用户对象
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null, NOT_LOGIN_ERROR);
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        ThrowUtils.throwIf(currentUser == null, NOT_LOGIN_ERROR);
        return currentUser;
    }


    /**
     * 用户登录方法
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request HTTP请求对象
     * @return 登录用户信息对象
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), PARAM_ERROR,"参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, PARAM_ERROR,"账号错误");
        ThrowUtils.throwIf(userPassword.length() < 8, PARAM_ERROR,"密码错误");
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(PARAM_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 4. 获得脱敏后的用户信息
        return this.getLoginUserVO(user);
    }


    /**
     * 用户注册方法
     * @param userAccount 用户账号
     * @param userEmail 用户邮箱
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 用户ID
     */
    @Override
    public long userRegister(String userAccount, String userEmail, String userPassword, String checkPassword) {
        // 参数校验：检查参数是否为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userEmail, userPassword, checkPassword), PARAM_ERROR,"参数不能为空");

        // 参数校验：账号长度不能小于4位
        ThrowUtils.throwIf(userAccount.length() < 4, PARAM_ERROR,"账号长度不能小于4位");
        // 参数校验：密码长度不能小于8位
        // 参数校验：两次输入的密码是否一致
        ThrowUtils.throwIf(userPassword.length() < 8, PARAM_ERROR,"密码长度不能小于8位");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), PARAM_ERROR,"两次密码不一致");

        //校验账号和邮箱是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        ThrowUtils.throwIf(count > 0, PARAM_ERROR,"账号已存在");

        //加密密码
        String encryptPassword=getEncryptPassword(userPassword);
        //插入数据
        User user = new User();
        user.setUserName(userAccount);
        user.setUserAccount(userAccount);
        user.setUserEmail(userEmail);
        user.setUserPassword(encryptPassword);
        user.setUserRole(UserRoleEnum.USER.getValue());

        boolean save = this.save(user);
        ThrowUtils.throwIf(!save,INTERNAL_ERROR,"注册失败");


        return user.getId();//返回用户id
    }


    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, PARAM_ERROR, "请求参数为空");
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String userEmail = userQueryRequest.getUserEmail();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userEmail", userEmail)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }




/**
 * 将User对象转换为UserVO对象
 * @param user 需要转换的User对象
 * @return 转换后的UserVO对象，如果输入为null则返回null
 */
    @Override
    public UserVO getUserVO(User user) {
    // 检查输入参数是否为null
        if (user == null) {
            return null;
        }
    // 创建新的UserVO对象
        UserVO userVO = new UserVO();
    // 使用BeanUtil工具类将User对象的属性复制到UserVO对象中
        BeanUtil.copyProperties(user, userVO);
    // 返回转换后的UserVO对象
        return userVO;
    }

/**
 * 将User对象列表转换为UserVO对象列表
 * @param userList User对象列表
 * @return UserVO对象列表，如果输入列表为空则返回空列表
 */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
    // 如果输入的用户列表为空，则返回一个新的空列表
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
    // 使用Stream将User列表转换为UserVO列表
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }




    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(attribute == null, DATA_OPERATION_FAIL,"用户未登录");
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取加密后的密码
     * @param userPassword 用户原始密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        //加盐：在密码前添加固定的盐值，增加密码的安全性
        final String SALT="lu"; // 定义盐值，这里使用固定的字符串"lu"
        // 将盐值和用户密码拼接后进行MD5加密，并返回加密后的十六进制字符串
        return DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
    }


}
