package com.team.weread.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rating",
        uniqueConstraints = @UniqueConstraint(name = "uk_rating_user_book", columnNames = {"User_ID", "Book_ID"}),
        indexes = {
                @Index(name = "idx_rating_user", columnList = "User_ID"),
                @Index(name = "idx_rating_book", columnList = "Book_ID")
        }
)
@Data
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Rating_ID")
    private Long ratingId;

    @Column(name = "User_ID", nullable = false)
    private Long userId;

    @Column(name = "Book_ID", nullable = false)
    private Long bookId;

    @Column(name = "Score", nullable = false, precision = 2, scale = 1)
    private BigDecimal score;

    @Column(name = "Comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "Status", nullable = false)
    private Byte status = 1;

    @CreationTimestamp
    @Column(name = "Rated_At", nullable = false, updatable = false)
    private LocalDateTime ratedAt;

    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "User_ID", referencedColumnName = "User_ID", insertable = false, updatable = false)
    private User user;

    @ManyToOne(targetEntity = Book.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "Book_ID", referencedColumnName = "Book_ID", insertable = false, updatable = false)
    private Book book;

    // 默认构造函数
    public Rating() {}

    // 全参构造函数
    public Rating(Long userId, Long bookId, BigDecimal score, String comment, Byte status) {
        this.userId = userId;
        this.bookId = bookId;
        this.score = score;
        this.comment = comment;
        this.status = status != null ? status : this.status;
    }
}