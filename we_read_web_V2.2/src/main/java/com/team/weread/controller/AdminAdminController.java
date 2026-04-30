package com.team.weread.controller;

import com.team.weread.model.Admin;
import com.team.weread.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/admins")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAdminController {

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        int p = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(p, size, Sort.by(Sort.Direction.DESC, "adminId"));
        Page<Admin> pg;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            List<Admin> all = adminRepository.findAll().stream().filter(a ->
                    (a.getUsername() != null && a.getUsername().toLowerCase().contains(kw)) ||
                    (a.getEmail() != null && a.getEmail().toLowerCase().contains(kw)) ||
                    (a.getRealName() != null && a.getRealName().toLowerCase().contains(kw))
            ).collect(Collectors.toList());
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), all.size());
            List<Admin> content = start <= end ? all.subList(start, end) : Collections.emptyList();
            pg = new PageImpl<>(content, pageable, all.size());
        } else {
            pg = adminRepository.findAll(pageable);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", pg.getContent().stream().map(this::toVO).collect(Collectors.toList()));
        data.put("total", pg.getTotalElements());
        data.put("currentPage", page);
        data.put("size", size);
        data.put("totalPages", pg.getTotalPages());
        return ResponseEntity.ok(Map.of("meta", meta(200, "获取成功"), "data", data));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String username = val(body.get("username"));
        String realname = val(body.get("realname"));
        String email = val(body.get("email"));
        String password = val(body.get("password"));
        if (username.isEmpty() || realname.isEmpty() || email.isEmpty() || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("meta", meta(400, "参数不合法"), "data", null));
        }
        if (adminRepository.findByUsername(username) != null) {
            return ResponseEntity.status(409).body(Map.of("meta", meta(409, "用户名已存在"), "data", null));
        }
        if (adminRepository.findByEmail(email) != null) {
            return ResponseEntity.status(409).body(Map.of("meta", meta(409, "邮箱已存在"), "data", null));
        }
        Admin a = new Admin();
        a.setUsername(username);
        a.setRealName(realname);
        a.setEmail(email);
        a.setPassword(passwordEncoder.encode(password));
        a.setStatusId(1);
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        a = adminRepository.save(a);
        return ResponseEntity.ok(Map.of("meta", meta(200, "创建成功"), "data", toVO(a)));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Admin a = adminRepository.findById(id).orElse(null);
        if (a == null) {
            return ResponseEntity.status(404).body(Map.of("meta", meta(404, "管理员不存在"), "data", null));
        }
        String realname = val(body.get("realname"));
        String email = val(body.get("email"));
        Integer statusId = null;
        if (body.get("statusId") != null) {
            try { statusId = Integer.parseInt(body.get("statusId").toString()); } catch (Exception ignored) {}
        }
        if (!email.isEmpty()) {
            Admin other = adminRepository.findByEmail(email);
            if (other != null && !other.getAdminId().equals(a.getAdminId())) {
                return ResponseEntity.status(409).body(Map.of("meta", meta(409, "邮箱已被占用"), "data", null));
            }
            a.setEmail(email);
        }
        if (!realname.isEmpty()) a.setRealName(realname);
        if (statusId != null) a.setStatusId(statusId);
        a.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(a);
        return ResponseEntity.ok(Map.of("meta", meta(200, "更新成功"), "data", toVO(a)));
    }

    @PutMapping("/{id}/password")
    @Transactional
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Admin a = adminRepository.findById(id).orElse(null);
        if (a == null) {
            return ResponseEntity.status(404).body(Map.of("meta", meta(404, "管理员不存在"), "data", null));
        }
        String np = val(body.get("newPassword"));
        if (np.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("meta", meta(400, "新密码至少6位"), "data", null));
        }
        a.setPassword(passwordEncoder.encode(np));
        a.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(a);
        return ResponseEntity.ok(Map.of("meta", meta(200, "密码已更新"), "data", null));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        if (!adminRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("meta", meta(404, "管理员不存在"), "data", null));
        }
        adminRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("meta", meta(200, "已删除"), "data", null));
    }

    private Map<String, Object> toVO(Admin a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getAdminId());
        m.put("username", a.getUsername());
        m.put("realname", a.getRealName());
        m.put("email", a.getEmail());
        m.put("statusId", a.getStatusId());
        m.put("createdAt", a.getCreatedAt());
        return m;
    }

    private Map<String, Object> meta(int code, String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("message", msg);
        return m;
    }

    private String val(Object o) {
        return o == null ? "" : o.toString().trim();
    }
}
