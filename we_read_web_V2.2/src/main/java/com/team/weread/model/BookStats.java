package com.team.weread.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_stats")
@Data
public class BookStats {

    @Id
    @Column(name = "Book_ID")
    private Long bookId;

    @Column(name = "View_Count", nullable = false)
    private int viewCount;

    @Column(name = "Download_Count", nullable = false)
    private int downloadCount;

    @Column(name = "Collection_Count", nullable = false)
    private int collectionCount;

    @Column(name = "Rating_Sum", nullable = false)
    private int ratingSum;

    @Column(name = "Rating_Count", nullable = false)
    private int ratingCount;

    @Column(name = "Updated_At", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(targetEntity = Book.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "Book_ID", referencedColumnName = "Book_ID", insertable = false, updatable = false)
    private Book book;

    // 默认构造函数
    public BookStats() {}

    // 全参构造函数
    public BookStats(Long bookID) {
        this.bookId = bookID;
        this.viewCount = 0;
        this.downloadCount = 0;
        this.collectionCount = 0;
        this.ratingSum = 0;
        this.ratingCount = 0;
        this.updatedAt = LocalDateTime.now();
    }


    // JPA 生命周期回调方法，用于自动更新 Updated_At 字段
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }
}