package com.team.weread.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "book",
        uniqueConstraints = @UniqueConstraint(name = "uk_book_isbn", columnNames = "ISBN"),
        indexes = {
                @Index(name = "idx_book_title", columnList = "Title"),
                @Index(name = "idx_book_publisher", columnList = "Publisher"),
                @Index(name = "idx_book_pubdate", columnList = "Publication_Date")
        }
)

@Data
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Book_ID")
    private Long bookId;

    @Column(name = "ISBN", nullable = false, length = 20)
    private String isbn;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "Publisher", length = 100)
    private String publisher;

    @Column(name = "Publication_Date")
    private LocalDate publicationDate;

    @Column(name = "Publish_Year")
    private Integer publishYear;

    @Column(name = "Language", length = 20)
    private String language;

    @Column(name = "Pages")
    private Integer pages;

    @Column(name = "Description", length = 65535)
    private String description;

    @Column(name = "Location", length = 100)
    private String location;

    @Column(name = "Call_Number", length = 100)
    private String callNumber;

    @Column(name = "Stock", columnDefinition = "INT DEFAULT 0")
    private Integer stock;

    @Column(name = "File_Path", length = 255)
    private String filePath;

    @Column(name = "Category_ID", nullable = false)
    private Long categoryId;

    @ManyToOne(targetEntity = Category.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "Category_ID", referencedColumnName = "Category_ID", insertable = false, updatable = false)
    private Category category;

    @Column(name = "Has_Ebook", nullable = false)
    private boolean hasEbook;

    @Column(name = "Status", nullable = false)
    private byte status;

    @Column(name = "Created_At", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_At", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "Rating", columnDefinition = "DECIMAL(3,2) DEFAULT 0.00")
    private Double rating;

    @Column(name = "Author", nullable = false, length = 200)
    private String author;



    @Column(name = "View_Count", columnDefinition = "INT DEFAULT 0")
    private Integer viewCount;

    @Column(name = "Collection_Count", columnDefinition = "INT DEFAULT 0")
    private Integer collectionCount;

    @Column(name = "Rating_Count", columnDefinition = "INT DEFAULT 0")
    private Integer ratingCount;

    @Column(name = "Is_Recommended", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean recommended;

    // 默认构造函数
    public Book() {}

    // 全参构造函数
    public Book(String isbn, String title, String publisher, LocalDate publicationDate, String language, Integer pages, String description, String filePath, Long categoryID, boolean hasEbook, byte status) {
        this.isbn = isbn;
        this.title = title;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.language = language;
        this.pages = pages;
        this.description = description;
        this.filePath = filePath;
        this.categoryId = categoryID;
        this.hasEbook = hasEbook;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.author = ""; // 设置默认值
    }

    public Double getAvgRating() {
        return rating != null ? rating : 0.0;
    }
}
