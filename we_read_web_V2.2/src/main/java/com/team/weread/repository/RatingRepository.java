package com.team.weread.repository;

import com.team.weread.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户评分仓库接口
 * <p>
 * 提供对Rating表的数据访问操作，包括基本的CRUD操作和自定义查询方法
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    /**
     * 根据用户ID和书籍ID查找评分记录
     *
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 评分记录，如果不存在则返回null
     */
    Rating findByUserIdAndBookId(Long userId, Long bookId);
    
    /**
     * 根据用户ID查找所有评分记录
     *
     * @param userId 用户ID
     * @return 评分记录列表
     */
    List<Rating> findAllByUserId(Long userId);
    
    /**
     * 根据书籍ID查找所有评分记录
     *
     * @param bookId 书籍ID
     * @return 评分记录列表
     */
    List<Rating> findAllByBookId(Long bookId);
    
    /**
     * 根据用户ID和状态查找评分记录
     *
     * @param userId 用户ID
     * @param status 状态：1-有效，0-已删除
     * @return 评分记录列表
     */
    List<Rating> findByUserIdAndStatus(Long userId, Byte status);
    
    /**
     * 根据书籍ID和状态查找评分记录
     *
     * @param bookId 书籍ID
     * @param status 状态：1-有效，0-已删除
     * @return 评分记录列表
     */
    List<Rating> findByBookIdAndStatus(Long bookId, Byte status);
}