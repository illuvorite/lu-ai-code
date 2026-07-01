package com.lu.luaicode.aop;

import cn.hutool.core.util.StrUtil;
import com.lu.luaicode.annotation.AuthCheck;
import com.lu.luaicode.model.dto.entity.User;
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
     * <p>
     * 这是一个使用@Around注解实现的AOP方法，用于在带有@AuthCheck注解的方法执行前后进行权限校验
     *
     * @param joinPoint 切入点，可以获取被拦截方法的信息并执行目标方法
     * @param authCheck 权限校验注解，包含权限信息
     * @return Object 目标方法的执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 从注解中获取必须的角色列表
        String[] mustRole = authCheck.mustRole();
        // 获取当前请求的属性
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        // 从请求属性中获取HttpServletRequest对象
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 获取当前用户具有的角色
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 没有有效的角色，拒绝
        if (userRoleEnum == null) {
            throw new BusinessException(ResultCode.NO_AUTH_ERROR);
        }
        // 遍历 mustRole 列表，用户匹配任意一个角色即可放行（OR 逻辑）
        for (String role : mustRole) {
            if (StrUtil.isBlank(role)) {
                continue;
            }
            UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(role);
            if (mustRoleEnum == null) {
                continue;
            }
            boolean hasPermission = switch (mustRoleEnum) {
                // 要求 ADMIN：ADMIN 或 SUPERADMIN 均可（SUPERADMIN 继承 ADMIN 权限）
                case ADMIN ->
                        UserRoleEnum.ADMIN.equals(userRoleEnum) || UserRoleEnum.SUPERADMIN.equals(userRoleEnum);
                // 要求 SUPERADMIN：仅 SUPERADMIN 可访问
                case SUPERADMIN -> UserRoleEnum.SUPERADMIN.equals(userRoleEnum);
                // 其他角色：精确匹配
                default -> mustRoleEnum.equals(userRoleEnum);
            };
            if (hasPermission) {
                return joinPoint.proceed();
            }
        }
        // 没有匹配任何角色，拒绝
        throw new BusinessException(ResultCode.NO_AUTH_ERROR);
    }
}
