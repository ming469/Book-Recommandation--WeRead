package com.team.weread.service;

import com.team.weread.dto.ChangePasswordRequest;
import com.team.weread.dto.LoginRequest;
import com.team.weread.model.User;
import org.springframework.data.domain.Page;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    User registerUser(User user);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    User findByEmail(String email);

    /**
     * 根据ID查找用户
     */
    User findById(Long id);

    /**
     * 用户登录
     */
    User login(LoginRequest loginRequest);

    /**
     * 更新用户信息
     */
    User updateUser(User user);

    /**
     * 修改用户密码
     */
    User changePassword(ChangePasswordRequest request);

    /**
     * 获取用户总数
     */
    long countTotalUsers();

    /**
     * 分页查询用户列表
     */
    Page<User> findUsers(int page, int size, String keyword);
}
