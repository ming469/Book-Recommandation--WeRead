package com.team.weread.service;

import com.team.weread.dto.LoginRequest;
import com.team.weread.model.Admin;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /**
     * 根据用户名查找管理员
     */
    Admin findByUsername(String username);

    /**
     * 根据邮箱查找管理员
     */
    Admin findByEmail(String email);

    /**
     * 用户登录
     */
    Admin login(LoginRequest loginRequest);

    /**
     * 更新管理员信息
     */
    Admin updateAdmin(Admin admin);

    /**
     * 根据ID查找管理员
     */
    Admin findById(Long id);

    /**
     * 验证管理员Token
     */
    boolean validateAdminToken(String token);
}
