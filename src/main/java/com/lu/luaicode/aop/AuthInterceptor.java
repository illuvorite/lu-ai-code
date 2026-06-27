package com.lu.luaicode.aop;

import com.lu.luaicode.annotation.AuthCheck;
import com.lu.luaicode.domain.entity.User;
import com.lu.luaicode.exception.BusinessException;
import com.lu.luaicode.exception.ResultCode;
import com.lu.luaicode.model.enums.UserRoleEnum;
import com.lu.luaicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *

 * 这是一个使用@Around注解实现的AOP方法，用于在带有@AuthCheck注解的方法执行前后进行权限校验
 *
     * @param joinPoint 切入点，可以获取被拦截方法的信息并执行目标方法
     * @param authCheck 权限校验注解，包含权限信息
 * @return Object 目标方法的执行结果
 * @throws Throwable 可能抛出的异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
    // 从注解中获取必须的角色权限
        String[] mustRole = authCheck.mustRole();
    // 获取当前请求的属性
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
    // 从请求属性中获取HttpServletRequest对象
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(Arrays.toString(mustRole));
        // 不需要权限，放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 以下为：必须有该权限才通过
        // 获取当前用户具有的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 没有权限，拒绝
        if (userRoleEnum == null) {
            throw new BusinessException(ResultCode.NO_AUTH_ERROR);
        }
        // 要求必须有管理员权限，但用户没有管理员或超级管理员权限，拒绝
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum) && !UserRoleEnum.SUPERADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ResultCode.NO_AUTH_ERROR);
        }
        //要求必须要有超级管理员权限 ，但用户没有超级管理员权限，拒绝
        if (UserRoleEnum.SUPERADMIN.equals(mustRoleEnum) && !UserRoleEnum.SUPERADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ResultCode.NO_AUTH_ERROR);
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}
