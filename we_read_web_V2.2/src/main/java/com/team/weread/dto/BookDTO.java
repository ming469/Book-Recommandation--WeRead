package com.team.weread.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图书数据传输对象
 */
@Data
public class BookDTO {
    private Long id;
    private String title;
    private String isbn;
    private String author;
    private String publisher;
    private LocalDate publicationDate;
    private Integer publishYear;
    private String language;
    private Integer pages;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Byte status;
    private Boolean hasEbook;
    private Integer viewCount;
    private Integer collectionCount;
    private Double ratingSum;
    private Integer ratingCount;
    private Double avgRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean recommended;
    private String location;
    private String callNumber;
    private Integer stock;
}
