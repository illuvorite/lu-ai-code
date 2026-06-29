package com.lu.luaicode.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户修改应用请求 DTO。
 *
 * @author illusory
 */
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
