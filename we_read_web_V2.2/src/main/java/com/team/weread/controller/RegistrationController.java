package com.team.weread.controller;

import com.team.weread.model.User;
import com.team.weread.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户注册控制器
 * <p>
 * 处理用户注册相关的请求
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.1
 */
@RestController
@RequestMapping("/api/user/auth")
public class RegistrationController {

    private final UserService userService;

    /**
     * 构造函数注入UserService
     *
     * @param userService 用户服务
     */
    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册接口
     * <p>
     * 处理用户注册请求，包括用户名和邮箱的唯一性校验
     * </p>
     *
     * @param user 用户注册信息
     * @return 注册结果响应
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) {
        // 检查用户名是否已存在
        User existingUserByUsername = userService.findByUsername(user.getUsername());
        if (existingUserByUsername != null) {
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> meta = new HashMap<>();
            meta.put("code", 409);
            meta.put("message", "用户名已存在");
            response.put("meta", meta);
            response.put("data", null);
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        // 检查邮箱是否已存在
        User existingUserByEmail = userService.findByEmail(user.getEmail());
        if (existingUserByEmail != null) {
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> meta = new HashMap<>();
            meta.put("code", 409);
            meta.put("message", "邮箱已被注册");
            response.put("meta", meta);
            response.put("data", null);
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        // 设置昵称为用户名
        user.setNickname(user.getUsername());

        // 注册新用户
        User registeredUser = userService.registerUser(user);

        // 构建响应数据
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", registeredUser.getId());
        userData.put("username", registeredUser.getUsername());
        userData.put("nickname", registeredUser.getNickname());
        userData.put("email", registeredUser.getEmail());

        Map<String, Object> meta = new HashMap<>();
        meta.put("code", 200);
        meta.put("message", "注册成功");

        Map<String, Object> response = new HashMap<>();
        response.put("meta", meta);
        response.put("data", userData);

        return ResponseEntity.ok(response);
    }
}
