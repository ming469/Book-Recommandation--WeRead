package com.team.weread.controller;

import com.team.weread.model.Review;
import com.team.weread.model.User;
import com.team.weread.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 书评控制器
 * <p>
 * 处理书评相关的请求，包括获取书评列表等功能
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private com.team.weread.service.UserService userService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user != null) {
                return user.getId();
            }
        }
        throw new IllegalStateException("用户未登录");
    }

    /**
     * 获取指定书籍的所有书评
     *
     * @param bookId 书籍ID
     * @return 书评列表
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<?> getBookReviews(@PathVariable Long bookId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("code", 200);
        meta.put("message", "获取成功");
        
        Map<String, Object> response = new HashMap<>();
        response.put("meta", meta);
        response.put("data", reviews);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前用户的所有书评
     *
     * @return 书评列表
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserReviews() {
        Long userId = getCurrentUserId();
        List<Review> reviews = reviewRepository.findByUserId(userId);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("code", 200);
        meta.put("message", "获取成功");
        
        Map<String, Object> response = new HashMap<>();
        response.put("meta", meta);
        response.put("data", reviews);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除指定书评
     *
     * @param reviewId 书评ID
     * @return 删除结果
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        Long userId = getCurrentUserId();
        Review review = reviewRepository.findById(reviewId).orElse(null);
        
        if (review == null) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("code", 404);
            meta.put("message", "书评不存在");
            
            Map<String, Object> response = new HashMap<>();
            response.put("meta", meta);
            response.put("data", null);
            
            return ResponseEntity.status(404).body(response);
        }
        
        // 验证是否是当前用户的书评
        if (!review.getUserId().equals(userId)) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("code", 403);
            meta.put("message", "无权删除他人书评");
            
            Map<String, Object> response = new HashMap<>();
            response.put("meta", meta);
            response.put("data", null);
            
            return ResponseEntity.status(403).body(response);
        }
        
        // 逻辑删除，将状态设置为0
        review.setStatus((byte) 0);
        reviewRepository.save(review);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("code", 200);
        meta.put("message", "删除成功");
        
        Map<String, Object> response = new HashMap<>();
        response.put("meta", meta);
        response.put("data", null);
        
        return ResponseEntity.ok(response);
    }
}