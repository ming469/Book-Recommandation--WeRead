package com.team.weread.controller;

import com.team.weread.dto.LoginRequest;
import com.team.weread.model.User;
import com.team.weread.repository.UserRepository;
import com.team.weread.service.UserService;
import com.team.weread.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户登录控制器
 * <p>
 * 处理用户登录认证相关的请求
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.1
 */
@RestController
@RequestMapping("/api/user/auth")
public class LoginController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * 构造函数注入依赖
     *
     * @param userService    用户服务
     * @param userRepository 用户仓库
     * @param jwtUtil        JWT工具类
     */
    @Autowired
    public LoginController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * 用户登录接口
     *
     * @param loginRequest 登录请求参数，包含账号、密码和是否记住登录状态
     * @return 登录结果响应，包含用户信息和JWT令牌
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody LoginRequest loginRequest) {
        User existingUser = userService.login(loginRequest);

        // 如果登录失败，构造失败响应
        if (existingUser == null) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("code", 401);
            meta.put("message", "用户名/邮箱或密码不正确");

            Map<String, Object> response = new HashMap<>();
            response.put("meta", meta);
            response.put("data", null);

            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // 根据rememberMe参数生成token
        boolean rememberMe = loginRequest.getRememberMe() != null && loginRequest.getRememberMe();
        String token = JwtUtil.generateToken(existingUser.getUsername(), existingUser.getId(), rememberMe);

        // 更新最新登录时间
        existingUser.setLastLoginTime(LocalDateTime.now());
        userRepository.save(existingUser);

        // 构造 meta 数据
        Map<String, Object> meta = new HashMap<>();
        meta.put("code", 200);
        meta.put("message", "登录成功");

        // 构造 user 数据
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", existingUser.getId());
        userData.put("username", existingUser.getUsername());

        // 构造 data 数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userData);

        // 最终构造返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("meta", meta);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> meta = new HashMap<>();
            meta.put("code", 401);
            meta.put("message", "用户未登录");
            response.put("meta", meta);
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> meta = new HashMap<>();
            meta.put("code", 401);
            meta.put("message", "用户未登录");
            response.put("meta", meta);
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = JwtUtil.generateToken(user.getUsername(), user.getId(), false);

        Map<String, Object> meta = new HashMap<>();
        meta.put("code", 200);
        meta.put("message", "刷新成功");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        Map<String, Object> response = new HashMap<>();
        response.put("meta", meta);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}
