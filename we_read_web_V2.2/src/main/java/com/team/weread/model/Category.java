package com.team.weread.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Category_ID")
    @JsonProperty("Category_ID")
    private Long categoryId;

    @ManyToOne(targetEntity = Category.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "Parent_ID", referencedColumnName = "Category_ID")
    @JsonIgnore
    private Category parent;

    @Column(name = "Name", nullable = false, length = 100)
    @JsonProperty("Name")
    private String name;

    @Column(name = "Description", length = 65535)
    @JsonProperty("Description")
    private String description;

    @Column(name = "Level", nullable = false)
    @JsonProperty("Level")
    private int level = 1;

    @Column(name = "Sort_Order", nullable = false)
    @JsonProperty("Sort_Order")
    private int sortOrder = 0;

    @Column(name = "Status", nullable = false)
    @JsonProperty("Status")
    private byte status = 1;

    @Column(name = "Created_At", nullable = false, updatable = false)
    @JsonProperty("Created_At")
    private LocalDateTime createdAt;

    @Column(name = "Updated_At")
    @JsonProperty("Updated_At")
    private LocalDateTime updatedAt;

    @org.hibernate.annotations.Formula("(SELECT COUNT(*) FROM book b WHERE b.Category_ID = Category_ID)")
    @JsonProperty("bookCount")
    private Long bookCount;

    @Transient
    @JsonProperty("children")
    private List<Category> children = new ArrayList<>();

    @Column(name = "Parent_ID", insertable = false, updatable = false)
    @JsonProperty("Parent_ID")
    private Long parentId = 0L;
    // 构造函数
    public Category() {}

    public Category(String name, String description, Category parent, int level, int sortOrder, byte status) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.level = level;
        this.sortOrder = sortOrder;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}
