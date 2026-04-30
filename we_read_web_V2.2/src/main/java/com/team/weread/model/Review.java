package com.team.weread.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "review",
        indexes = {
                @Index(name = "idx_review_user", columnList = "User_ID"),
                @Index(name = "idx_review_book", columnList = "Book_ID")
        }
)
@Data
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Review_ID")
    private Long reviewId;

    @Column(name = "User_ID", nullable = false)
    private Long userId;

    @Column(name = "Book_ID", nullable = false)
    private Long bookId;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "Content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "Status", nullable = false)
    private Byte status = 1;

    @CreationTimestamp
    @Column(name = "Created_At", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "Updated_At", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "User_ID", referencedColumnName = "User_ID", insertable = false, updatable = false)
    private User user;

    @ManyToOne(targetEntity = Book.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "Book_ID", referencedColumnName = "Book_ID", insertable = false, updatable = false)
    private Book book;

    // 默认构造函数
    public Review() {}

    // 全参构造函数
    public Review(Long userId, Long bookId, String title, String content, Byte status) {
        this.userId = userId;
        this.bookId = bookId;
        this.title = title;
        this.content = content;
        this.status = status != null ? status : this.status;
    }
}