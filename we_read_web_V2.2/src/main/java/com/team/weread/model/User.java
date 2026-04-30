package com.team.weread.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "User_ID")
    private Long id;

    @Column(name = "Username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "Nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "Password", nullable = false, length = 255)
    private String password;

    @Column(name = "Email", nullable = false, length = 100)
    private String email;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "Avatar", length = 255)
    private String avatar;

    @Column(name = "Gender", nullable = false)
    private Integer gender = 0;

    @Column(name = "Preference", columnDefinition = "json")
    private String preference;

    @Column(name = "Status", nullable = false)
    private Integer status = 1;

    @Column(name = "Last_Login_Time")
    private LocalDateTime lastLoginTime;

    @CreationTimestamp
    @Column(name = "Created_At", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "Updated_At", nullable = false)
    private LocalDateTime updatedAt;

    public User() {
    }

    public User(String username, String nickname, String password, String email, String phone, String avatar,
                Integer gender, String preference, Integer status) {
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        this.gender = gender != null ? gender : this.gender;
        this.preference = preference;
        this.status = status != null ? status : this.status;
    }
}