package com.team.weread.service;

import java.util.List;
import java.util.Map;

/**
 * 图书推荐服务接口
 */
public interface BookRecommendService {

    /**
     * 记录用户行为
     *
     * @param userId       用户ID
     * @param bookId       图书ID
     * @param categoryId   分类ID
     * @param behaviorType 行为类型
     * @param score        行为得分
     */
    void recordUserBehavior(Long userId, Long bookId, Long categoryId, String behaviorType, Double score);

    /**
     * 获取用户的分类偏好
     *
     * @param userId 用户ID
     * @return 分类权重映射（分类ID -> 权重分数）
     */
    Map<Long, Double> getUserCategoryPreferences(Long userId);

    /**
     * 根据用户偏好获取推荐图书列表
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 推荐图书ID列表
     */
    List<Long> getRecommendedBooks(Long userId, int limit);

    /**
     * 同步所有历史行为数据到 user_behavior 表
     * 
     * @return 同步成功的记录数
     */
    int syncAllBehaviors();

    /**
     * 清理所有用户行为数据
     */
    void clearAllBehaviors();

    /**
     * 计算并存储物品相似度
     */
    void calculateAndStoreSimilarities();

    /**
     * 获取用户行为总数
     * 
     * @return 行为记录总数
     */
    long countAllBehaviors();
}