package com.team.weread.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import java.time.LocalDateTime;

@Entity
@Table(name = "Admin", uniqueConstraints = {
        @UniqueConstraint(name = "uk_admin_username", columnNames = "Username"),
        @UniqueConstraint(name = "uk_admin_email", columnNames = "Email")
})


@Data
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Admin_ID", nullable = false)
    private Long adminId;

    @Column(name = "Username", nullable = false, length = 50, unique = true)
    @NotBlank(message = "管理员登录名不能为空")
    private String username;

    @Column(name = "Real_Name", nullable = false, length = 50)
    @NotBlank(message = "管理员真实姓名不能为空")
    private String realName;

    @Column(name = "Password", nullable = false, length = 255)
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 255, message = "密码长度必须在6到255之间")
    private String password;

    @Column(name = "Email", nullable = false, length = 100, unique = true)
    @NotBlank(message = "电子邮箱不能为空")
    @Email(message = "电子邮箱格式不正确")
    private String email;

    @Column(name = "Status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer statusId;

    @Column(name = "Created_At", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "Updated_At", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
 }
