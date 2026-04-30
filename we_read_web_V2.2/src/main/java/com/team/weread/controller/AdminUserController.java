package com.team.weread.controller;

import com.team.weread.model.User;
import com.team.weread.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    private Map<String, Object> buildMeta(int code, String message) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("code", code);
        meta.put("message", message);
        return meta;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase();
            users = userRepository.findAll(pageable)
                    .map(u -> u); // keep pageable structure
            // Filter content while keeping total as filtered count
            var filtered = users.getContent().stream()
                    .filter(u ->
                            (u.getUsername() != null && u.getUsername().toLowerCase().contains(k)) ||
                            (u.getEmail() != null && u.getEmail().toLowerCase().contains(k))
                    ).toList();
            int start = Math.min((int) pageable.getOffset(), filtered.size());
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            users = new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
        } else {
            users = userRepository.findAll(pageable);
        }

        var content = users.getContent().stream().map(u -> Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "email", u.getEmail(),
                "createdAt", u.getCreatedAt()
        )).toList();

        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("totalElements", users.getTotalElements());
        data.put("totalPages", users.getTotalPages());
        data.put("currentPage", users.getNumber() + 1);
        data.put("size", users.getSize());

        Map<String, Object> resp = new HashMap<>();
        resp.put("meta", buildMeta(200, "获取用户列表成功"));
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}/password")
    @Transactional
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("meta", buildMeta(400, "新密码不合法，至少6位"));
            resp.put("data", null);
            return ResponseEntity.badRequest().body(resp);
        }
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("meta", buildMeta(404, "用户不存在"));
            resp.put("data", null);
            return ResponseEntity.status(404).body(resp);
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        Map<String, Object> resp = new HashMap<>();
        resp.put("meta", buildMeta(200, "密码已重置"));
        resp.put("data", null);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        if (!userRepository.existsById(id)) {
            resp.put("meta", buildMeta(404, "用户不存在"));
            resp.put("data", null);
            return ResponseEntity.status(404).body(resp);
        }
        try {
            userRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            resp.put("meta", buildMeta(409, "用户存在关联数据，无法删除"));
            resp.put("data", null);
            return ResponseEntity.status(409).body(resp);
        }
        resp.put("meta", buildMeta(200, "用户已删除"));
        resp.put("data", null);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/batch-password")
    @Transactional
    public ResponseEntity<Map<String, Object>> batchResetPassword(@RequestBody Map<String, Object> body) {
        Object idsObj = body.get("ids");
        Object npObj = body.get("newPassword");
        if (!(idsObj instanceof List<?> ids) || ids.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("meta", buildMeta(400, "ids 参数无效"));
            resp.put("data", null);
            return ResponseEntity.badRequest().body(resp);
        }
        String newPassword = npObj != null ? npObj.toString() : null;
        if (newPassword == null || newPassword.length() < 6) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("meta", buildMeta(400, "新密码不合法，至少6位"));
            resp.put("data", null);
            return ResponseEntity.badRequest().body(resp);
        }
        int success = 0;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        for (Object o : ids) {
            try {
                Long uid = Long.valueOf(o.toString());
                User user = userRepository.findById(uid).orElse(null);
                if (user != null) {
                    user.setPassword(encoder.encode(newPassword));
                    userRepository.save(user);
                    success++;
                }
            } catch (Exception ignored) {}
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("meta", buildMeta(200, "批量密码重置完成"));
        resp.put("data", Map.of("success", success, "total", ((List<?>) idsObj).size()));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/batch-password/test")
    @Transactional
    public ResponseEntity<Map<String, Object>> batchResetTestUsers(@RequestBody Map<String, Object> body) {
        Object npObj = body.get("newPassword");
        String newPassword = npObj != null ? npObj.toString() : null;
        if (newPassword == null || newPassword.length() < 6) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("meta", buildMeta(400, "新密码不合法，至少6位"));
            resp.put("data", null);
            return ResponseEntity.badRequest().body(resp);
        }
        List<User> testUsers = userRepository.findByUsernameStartingWith("user_");
        int success = 0;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        for (User user : testUsers) {
            try {
                user.setPassword(encoder.encode(newPassword));
                userRepository.save(user);
                success++;
            } catch (Exception ignored) {}
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("meta", buildMeta(200, "测试用户批量密码重置完成"));
        resp.put("data", Map.of("success", success, "total", testUsers.size()));
        return ResponseEntity.ok(resp);
    }
}
