package com.team.weread.repository;

import com.team.weread.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 书评仓库接口
 * <p>
 * 提供对Review实体的数据库操作
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * 根据用户ID查找书评
     *
     * @param userId 用户ID
     * @return 用户的所有书评列表
     */
    List<Review> findByUserId(Long userId);
    
    /**
     * 根据书籍ID查找书评
     *
     * @param bookId 书籍ID
     * @return 书籍的所有书评列表
     */
    List<Review> findByBookId(Long bookId);
    
    /**
     * 根据用户ID和书籍ID查找书评
     *
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 用户对特定书籍的所有书评列表
     */
    List<Review> findByUserIdAndBookId(Long userId, Long bookId);
}