package com.team.weread.repository;

import com.team.weread.model.BrowsingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 浏览历史数据访问接口
 * <p>
 * 提供对用户浏览历史记录的数据库操作
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@Repository
public interface BrowsingHistoryRepository extends JpaRepository<BrowsingHistory, Long> {

    /**
     * 根据用户ID查询浏览历史
     *
     * @param userId 用户ID
     * @return 浏览历史列表
     */
    List<BrowsingHistory> findByUserId(Long userId);
    
    /**
     * 根据用户ID分页查询浏览历史
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分页浏览历史
     */
    Page<BrowsingHistory> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID和书籍ID查询浏览记录
     *
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 浏览记录
     */
    BrowsingHistory findByUserIdAndBookId(Long userId, Long bookId);
    
    /**
     * 根据用户ID查询最近浏览的书籍记录
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 最近浏览的书籍记录
     */
    @Query("SELECT bh FROM BrowsingHistory bh WHERE bh.userId = :userId ORDER BY bh.viewedAt DESC")
    Page<BrowsingHistory> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 根据用户ID和时间范围查询浏览历史
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间范围内的浏览历史
     */
    List<BrowsingHistory> findByUserIdAndViewedAtBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据用户ID和来源查询浏览历史
     *
     * @param userId 用户ID
     * @param source 来源
     * @return 特定来源的浏览历史
     */
    List<BrowsingHistory> findByUserIdAndSource(Long userId, String source);
    
    /**
     * 删除用户的所有浏览历史
     *
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);
    
    /**
     * 删除用户对特定书籍的浏览记录
     *
     * @param userId 用户ID
     * @param bookId 书籍ID
     */
    void deleteByUserIdAndBookId(Long userId, Long bookId);
}