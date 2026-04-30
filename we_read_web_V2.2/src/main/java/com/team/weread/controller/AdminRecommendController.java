package com.team.weread.controller;

import com.team.weread.model.Book;
import com.team.weread.service.BookRecommendService;
import com.team.weread.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/recommend")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecommendController {

    @Autowired
    private BookRecommendService bookRecommendService;
    @Autowired
    private BookService bookService;

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncAllBehaviors() {
        int count = bookRecommendService.syncAllBehaviors();
        Map<String, Object> resp = new HashMap<>();
        resp.put("meta", meta(200, "同步完成"));
        resp.put("data", Map.of("synced", count));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/recompute")
    public ResponseEntity<Map<String, Object>> recomputeSimilarities() {
        bookRecommendService.calculateAndStoreSimilarities();
        Map<String, Object> resp = new HashMap<>();
        resp.put("meta", meta(200, "相似度计算已触发"));
        resp.put("data", Map.of("started", true));
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/preferences/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPreferences(@PathVariable Long userId) {
        Map<Long, Double> pref = bookRecommendService.getUserCategoryPreferences(userId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("meta", meta(200, "获取用户分类偏好成功"));
        resp.put("data", pref);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/preview")
    public ResponseEntity<Map<String, Object>> preview(@RequestParam Long userId,
                                                       @RequestParam(defaultValue = "20") int limit) {
        List<Long> ids = bookRecommendService.getRecommendedBooks(userId, limit);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Long id : ids) {
            Book b = bookService.getBookById(id);
            if (b != null) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", b.getBookId());
                m.put("title", b.getTitle());
                m.put("author", b.getAuthor());
                m.put("categoryId", b.getCategoryId());
                list.add(m);
            }
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("meta", meta(200, "获取预览推荐成功"));
        resp.put("data", list);
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> meta(int code, String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("message", msg);
        return m;
    }
}
