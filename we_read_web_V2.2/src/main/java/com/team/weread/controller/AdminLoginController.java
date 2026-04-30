package com.team.weread.controller;
import com.team.weread.dto.LoginRequest;
import com.team.weread.model.Admin;

import com.team.weread.repository.AdminRepository;

import com.team.weread.service.AdminService;

import com.team.weread.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员登录控制器
 * <p>
 * 处理管理员登录认证相关的请求
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminLoginController {

    private final AdminService adminService;
    private final AdminRepository adminRepository;

    /**
     * 构造函数注入依赖
     *
     * @param adminService    管理员服务
     * @param adminRepository 管理员仓库
     * @param jwtUtil        JWT工具类
     */
    @Autowired
    public AdminLoginController(AdminService adminService, AdminRepository adminRepository) {
        this.adminService = adminService;
        this.adminRepository = adminRepository;
    }

    /**
     * 管理员登录接口
     *
     * @param loginRequest 登录请求参数，包含账号、密码和是否记住登录状态
     * @return 登录结果响应，包含管理员信息和JWT令牌
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginAdmin(@RequestBody LoginRequest loginRequest) {
        Admin existingAdmin = adminService.login(loginRequest);

        // 如果登录失败，构造失败响应
        if (existingAdmin == null) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("code", 401);
            meta.put("message", "用户名/邮箱或密码不正确");

            Map<String, Object> response = new HashMap<>();
            response.put("meta", meta);
            response.put("data", null);

            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // 使用JWT工具类生成token，考虑rememberMe参数
        boolean rememberMe = loginRequest.getRememberMe() != null && loginRequest.getRememberMe();
        String token = JwtUtil.generateToken(
                existingAdmin.getUsername(),
                existingAdmin.getAdminId(),
                rememberMe
        );

        // 更新最新登录时间
        existingAdmin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(existingAdmin);

        // 构建用户数据
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("id", existingAdmin.getAdminId());
        adminData.put("username", existingAdmin.getUsername());
        adminData.put("realname", existingAdmin.getRealName());
        adminData.put("email", existingAdmin.getEmail());
        

        // 构造 meta 数据
        Map<String, Object> meta = new HashMap<>();
        meta.put("code", 200);
        meta.put("message", "登录成功");

        // 构造 data 数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("admin", adminData);

        // 最终构造返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("meta", meta);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
