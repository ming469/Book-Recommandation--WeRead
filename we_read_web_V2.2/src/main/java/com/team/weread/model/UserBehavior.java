package com.team.weread.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户行为记录实体类
 */
@Data
@Entity
@Table(name = "user_behavior", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_book", columnNames = {"userId", "bookId"})
}, indexes = {
    @Index(name = "idx_user", columnList = "userId"),
    @Index(name = "idx_book", columnList = "bookId"),
    @Index(name = "idx_category", columnList = "categoryId")
})
public class UserBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 图书ID
     */
    @Column(nullable = false)
    private Long bookId;

    /**
     * 图书分类ID
     */
    @Column
    private Long categoryId;

    /**
     * 行为类型 (VIEW, CLICK, COLLECT, ADD_SHELF, RATE, REVIEW)
     */
    @Column(nullable = false, length = 20)
    private String behaviorType;

    /**
     * 行为得分
     */
    @Column(nullable = false)
    private Double score;

    /**
     * 行为时间
     */
    @Column(nullable = false)
    private LocalDateTime createTime;

    // 行为类型常量定义
    public static final String VIEW = "VIEW";
    public static final String CLICK = "CLICK";
    public static final String COLLECT = "COLLECT";
    public static final String ADD_SHELF = "ADD_SHELF";
    public static final String RATE = "RATE";
    public static final String REVIEW = "REVIEW";

    // 行为评分常量定义
    public static final Double SCORE_VIEW = 1.0;
    public static final Double SCORE_CLICK = 0.5;
    public static final Double SCORE_COLLECT = 3.0;
    public static final Double SCORE_ADD_SHELF = 4.0;
    public static final Double SCORE_RATE = 5.0; // 实际由用户输入决定，此处为默认映射参考
    public static final Double SCORE_REVIEW = 4.0;
}
