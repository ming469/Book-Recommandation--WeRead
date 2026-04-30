package com.team.weread.controller;

import com.team.weread.model.Admin;
import com.team.weread.repository.AdminRepository;
import com.team.weread.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/profile")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfileController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile() {
        Admin admin = currentAdmin();
        if (admin == null) {
            return ResponseEntity.status(401).body(Map.of("meta", meta(401, "未登录"), "data", null));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", admin.getAdminId());
        data.put("username", admin.getUsername());
        data.put("realname", admin.getRealName());
        data.put("email", admin.getEmail());
        data.put("createdAt", admin.getCreatedAt());
        return ResponseEntity.ok(Map.of("meta", meta(200, "获取成功"), "data", data));
    }

    @PutMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> body) {
        Admin admin = currentAdmin();
        if (admin == null) {
            return ResponseEntity.status(401).body(Map.of("meta", meta(401, "未登录"), "data", null));
        }
        String realname = body.get("realname") != null ? body.get("realname").toString().trim() : null;
        String email = body.get("email") != null ? body.get("email").toString().trim() : null;
        if (realname != null && !realname.isEmpty()) admin.setRealName(realname);
        if (email != null && !email.isEmpty()) {
            Admin other = adminRepository.findByEmail(email);
            if (other != null && !other.getAdminId().equals(admin.getAdminId())) {
                return ResponseEntity.status(409).body(Map.of("meta", meta(409, "邮箱已被占用"), "data", null));
            }
            admin.setEmail(email);
        }
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);
        return ResponseEntity.ok(Map.of("meta", meta(200, "更新成功"), "data", null));
    }

    @PutMapping("/password")
    @Transactional
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> body) {
        Admin admin = currentAdmin();
        if (admin == null) {
            return ResponseEntity.status(401).body(Map.of("meta", meta(401, "未登录"), "data", null));
        }
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("meta", meta(400, "新密码不合法，至少6位"), "data", null));
        }
        if (oldPassword == null || !passwordEncoder.matches(oldPassword, admin.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("meta", meta(401, "旧密码不正确"), "data", null));
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);
        return ResponseEntity.ok(Map.of("meta", meta(200, "密码已更新"), "data", null));
    }

    private Admin currentAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return adminService.findByUsername(auth.getName());
    }

    private Map<String, Object> meta(int code, String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("message", msg);
        return m;
    }
}
