package com.team.weread.repository;

import com.team.weread.model.UserBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 用户行为数据访问接口
 */
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {

    /**
     * 查询用户的行为记录
     *
     * @param userId 用户ID
     * @return 用户行为记录列表
     */
    List<UserBehavior> findByUserId(Long userId);

    /**
     * 根据用户ID和图书ID查询行为记录
     * 
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 行为记录
     */
    UserBehavior findByUserIdAndBookId(Long userId, Long bookId);

    /**
     * 统计用户对各个分类的行为得分总和
     *
     * @param userId 用户ID
     * @return 分类得分列表
     */
    @Query("SELECT ub.categoryId, SUM(ub.score) as totalScore " +
           "FROM UserBehavior ub " +
           "WHERE ub.userId = :userId " +
           "GROUP BY ub.categoryId " +
           "ORDER BY totalScore DESC")
    List<Object[]> findCategoryScoresByUserId(@Param("userId") Long userId);
}