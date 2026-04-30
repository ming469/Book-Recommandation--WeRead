package com.team.weread.dto;

import lombok.Data;

/**
 * 登录请求数据传输对象
 * <p>
 * 用于接收前端登录请求的参数
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@Data
public class LoginRequest {

    /**
     * 账号，可以是用户名或邮箱
     */
    private String account;

    /**
     * 密码
     */
    private String password;

    /**
     * 是否记住登录状态
     */
    private Boolean rememberMe;
}
