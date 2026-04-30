package com.team.weread.repository;

import com.team.weread.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    /**
     * 根据用户名查找管理员
     *
     * @param username 用户名
     * @return 符合条件的管理员，如果不存在则返回null
     */
    Admin findByUsername(String username);

    /**
     * 根据邮箱查找管理员
     *
     * @param email 邮箱
     * @return 符合条件的管理员，如果不存在则返回null
     */
    Admin findByEmail(String email);
}
