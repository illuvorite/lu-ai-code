package com.lu.luaicode.model.dto.user;

import com.lu.luaicode.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * UserQueryRequest类，用于封装用户查询请求参数
 * 继承自PageRequest类并实现Serializable接口，支持序列化操作
 * 使用@Data和@EqualsAndHashCode注解简化代码，自动生成getter、setter、equals和hashCode方法
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban/superadmin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
