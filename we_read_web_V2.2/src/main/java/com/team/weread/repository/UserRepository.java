package com.team.weread.repository;

import com.team.weread.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

/**
 * 用户数据访问接口
 * <p>
 * 提供用户相关的数据库操作方法
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 符合条件的用户，如果不存在则返回null
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 符合条件的用户，如果不存在则返回null
     */
    User findByEmail(String email);
    
    /**
     * 查询指定前缀的用户名用户列表
     */
    List<User> findByUsernameStartingWith(String prefix);
}
