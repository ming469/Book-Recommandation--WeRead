package com.team.weread.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/admin/system")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemController {

    private static final Map<String, Object> MEM_CONFIG = new HashMap<>();

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("allowRegister", MEM_CONFIG.getOrDefault("allowRegister", Boolean.TRUE));
        cfg.put("pageSizeDefault", MEM_CONFIG.getOrDefault("pageSizeDefault", 10));
        cfg.put("recommend.seedLimit", MEM_CONFIG.getOrDefault("recommend.seedLimit", 20));
        cfg.put("recommend.topK", MEM_CONFIG.getOrDefault("recommend.topK", 80));
        cfg.put("recommend.alpha", MEM_CONFIG.getOrDefault("recommend.alpha", 0.2));
        cfg.put("recommend.categoryCapRatio", MEM_CONFIG.getOrDefault("recommend.categoryCapRatio", 0.4));
        Map<String, Object> resp = new HashMap<>();
        resp.put("meta", meta(200, "获取配置成功"));
        resp.put("data", cfg);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/config")
    public ResponseEntity<Map<String, Object>> updateConfig(@RequestBody Map<String, Object> body) {
        if (body != null) {
            MEM_CONFIG.putAll(body);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("meta", meta(200, "配置已更新"));
        resp.put("data", MEM_CONFIG);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/cache/evict")
    public ResponseEntity<Map<String, Object>> evictCache(@RequestParam("pattern") String pattern) {
        int removed = 0;
        if (stringRedisTemplate != null) {
            try {
                Set<String> keys = stringRedisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    stringRedisTemplate.delete(keys);
                    removed = keys.size();
                }
            } catch (Exception ignored) {}
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("meta", meta(200, "清理完成"));
        resp.put("data", Map.of("removed", removed, "pattern", pattern, "redis", stringRedisTemplate != null));
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/redis/ping")
    public ResponseEntity<Map<String, Object>> redisPing() {
        boolean connected = false;
        String pong = null;
        if (stringRedisTemplate != null) {
            try {
                pong = stringRedisTemplate.getRequiredConnectionFactory().getConnection().ping();
                connected = pong != null;
            } catch (Exception ignored) {}
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("meta", meta(200, "OK"));
        resp.put("data", Map.of("connected", connected, "pong", pong));
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> meta(int code, String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("message", message);
        return m;
    }
}
