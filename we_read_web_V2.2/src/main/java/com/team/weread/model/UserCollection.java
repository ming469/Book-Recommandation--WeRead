package com.team.weread.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏/书架实体类
 * <p>
 * 对应数据库中的User_Collection表，存储用户收藏的书籍信息
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_collection")
@IdClass(UserCollectionPK.class)
@Data
public class UserCollection implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Collection_ID")
    private Long collectionId;

    @Id
    @Column(name = "User_ID")
    private Long userId;

    @Id
    @Column(name = "Book_ID")
    private Long bookId;

    @Id
    @Column(name = "Collection_Type", nullable = false)
    private Byte collectionType = 3;

    @Column(name = "Note", length = 255)
    private String note;

    @CreationTimestamp
    @Column(name = "Created_At", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "User_ID", referencedColumnName = "User_ID", insertable = false, updatable = false)
    private User user;


    @ManyToOne(targetEntity = Book.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "Book_ID", referencedColumnName = "Book_ID", insertable = false, updatable = false)
    private Book book;

    // 默认构造函数
    public UserCollection() {}

    // 全参构造函数
    public UserCollection(Long userId, Long bookId, Byte collectionType, String note) {
        this.userId = userId;
        this.bookId = bookId;
        this.collectionType = collectionType;
        this.note = note;
    }


}