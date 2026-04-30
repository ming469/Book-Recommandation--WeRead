package com.team.weread.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;     // primary, success, warning, danger, info
    private String content;  // 活动标题
    private String detail;   // 活动详情
    private LocalDateTime time = LocalDateTime.now();
    private boolean hollow = false;
}
