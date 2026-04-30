package com.team.weread.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "browsing_history",
        indexes = {
                @Index(name = "idx_browsing_history_user_book", columnList = "User_ID, Book_ID"),
                @Index(name = "idx_browsing_history_time", columnList = "User_ID, Viewed_At")
        }
)
@Data
public class BrowsingHistory {

    /**
     * 历史记录ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "History_ID")
    private Long historyId;

    /**
     * 用户ID，外键
     */
    @Column(name = "User_ID", nullable = false)
    private Long userId;

    /**
     * 书籍ID，外键
     */
    @Column(name = "Book_ID", nullable = false)
    private Long bookId;

    /**
     * 浏览来源
     */
    @Column(name = "Source", length = 50)
    private String source;

    /**
     * 浏览时间
     */
    @CreationTimestamp
    @Column(name = "Viewed_At", nullable = false, updatable = false)
    private LocalDateTime viewedAt;

    /**
     * 关联的用户实体
     */
    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "User_ID", referencedColumnName = "User_ID", insertable = false, updatable = false)
    private User user;

    /**
     * 关联的书籍实体
     */
    @ManyToOne(targetEntity = Book.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "Book_ID", referencedColumnName = "Book_ID", insertable = false, updatable = false)
    private Book book;

    // 默认构造函数
    public BrowsingHistory() {}

    // 全参构造函数
    public BrowsingHistory(Long userId, Long bookId, String source) {
        this.userId = userId;
        this.bookId = bookId;
        this.source = source;
    }
}