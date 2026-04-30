package com.team.weread.controller;

import com.team.weread.model.UserBehavior;
import com.team.weread.model.Book;
import com.team.weread.model.BookStats;
import com.team.weread.model.BrowsingHistory;
import com.team.weread.model.Rating;
import com.team.weread.model.Review;
import com.team.weread.model.User;
import com.team.weread.model.UserCollection;
import com.team.weread.dto.BookReviewRequest;
import com.team.weread.dto.UserBehaviorRequest;
import com.team.weread.repository.BookStatsRepository;
import com.team.weread.repository.BrowsingHistoryRepository;
import com.team.weread.repository.RatingRepository;
import com.team.weread.repository.ReviewRepository;
import com.team.weread.repository.UserCollectionRepository;
import com.team.weread.repository.UserBehaviorRepository;
import com.team.weread.service.BookRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
// 导入请求类

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户行为控制器
 * <p>
 * 处理用户行为记录相关的请求，包括记录用户行为、获取用户偏好和推荐图书
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/user/behavior")
public class UserBehaviorController {

    @Autowired
    private BookRecommendService bookRecommendService;
    
    @Autowired
    private BookStatsRepository bookStatsRepository;
    
    @Autowired
    private BrowsingHistoryRepository browsingHistoryRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserCollectionRepository userCollectionRepository;
    
    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private com.team.weread.service.UserService userService;
    
    @Autowired
    private com.team.weread.service.BookService bookService;
    
    @Autowired
    private UserBehaviorRepository userBehaviorRepository;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            return user != null ? user.getId() : null;
        }
        return null;
    }

    /**
     * 记录用户浏览行为 (VIEW)
     */
    @PostMapping("/view")
    @Transactional
    public ResponseEntity<Map<String, Object>> recordViewBehavior(@RequestBody UserBehaviorRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return buildUnauthorizedResponse();
        }
        Long bookId = request.getBookId();
        Long categoryId = request.getCategoryId();
        
        // 1. 记录用户行为到UserBehavior表 (VIEW, score=1.0)
        bookRecommendService.recordUserBehavior(
            userId,
            bookId,
            categoryId,
            UserBehavior.VIEW,
            UserBehavior.SCORE_VIEW
        );
        
        // 2. 更新图书统计信息，增加浏览次数
        updateBookViewStats(bookId);
        
        // 3. 添加浏览历史记录
        BrowsingHistory history = new BrowsingHistory(userId, bookId, "web");
        browsingHistoryRepository.save(history);
        
        return buildSuccessResponse("记录浏览成功", null);
    }

    /**
     * 记录用户点击行为 (CLICK)
     */
    @PostMapping("/click")
    @Transactional
    public ResponseEntity<Map<String, Object>> recordClickBehavior(@RequestBody UserBehaviorRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return buildUnauthorizedResponse();
        }
        Long bookId = request.getBookId();
        Long categoryId = request.getCategoryId();
        
        // 1. 记录用户行为到UserBehavior表 (CLICK, score=2.0)
        bookRecommendService.recordUserBehavior(
            userId,
            bookId,
            categoryId,
            UserBehavior.CLICK,
            UserBehavior.SCORE_CLICK
        );
        
        // 2. 更新图书统计信息，增加浏览次数
        updateBookViewStats(bookId);
        
        // 3. 添加浏览历史记录
        BrowsingHistory history = new BrowsingHistory(userId, bookId, "web");
        browsingHistoryRepository.save(history);
        
        return buildSuccessResponse("记录点击成功", null);
    }
    
    /**
     * 记录用户收藏行为 (COLLECT)
     */
    @PostMapping("/favorite")
    @Transactional
    public ResponseEntity<Map<String, Object>> recordFavoriteBehavior(@RequestBody UserBehaviorRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return buildUnauthorizedResponse();
        }
        Long bookId = request.getBookId();
        Long categoryId = request.getCategoryId();
        
        // 1. 记录用户行为到UserBehavior表 (COLLECT, score=3.0)
        bookRecommendService.recordUserBehavior(
            userId,
            bookId,
            categoryId,
            UserBehavior.COLLECT,
            UserBehavior.SCORE_COLLECT
        );
        
        // 2. 更新图书统计信息，增加收藏次数
        BookStats bookStats = bookStatsRepository.findByBookId(bookId);
        if (bookStats != null) {
            bookStats.setCollectionCount(bookStats.getCollectionCount() + 1);
            bookStats.setUpdatedAt(LocalDateTime.now());
            bookStatsRepository.save(bookStats);
        }
        
        // 3. 保存用户收藏记录到UserCollection表 (类型3: 已读/收藏)
        saveUserCollection(userId, bookId, (byte) 3);
        
        return buildSuccessResponse("记录收藏成功", null);
    }

    /**
     * 记录用户加入书架行为 (ADD_SHELF)
     */
    @PostMapping("/shelf")
    @Transactional
    public ResponseEntity<Map<String, Object>> recordShelfBehavior(@RequestBody UserBehaviorRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return buildUnauthorizedResponse();
        }
        Long bookId = request.getBookId();
        Long categoryId = request.getCategoryId();
        if (categoryId == null) {
            com.team.weread.model.Book b = bookService.getBookById(bookId);
            categoryId = (b != null) ? b.getCategoryId() : null;
        }
        
        // 1. 记录用户行为到UserBehavior表 (ADD_SHELF, score=4.0)
        bookRecommendService.recordUserBehavior(
            userId,
            bookId,
            categoryId,
            UserBehavior.ADD_SHELF,
            UserBehavior.SCORE_ADD_SHELF
        );
        
        // 2. 保存用户书架记录到UserCollection表 (类型2: 在读/书架)
        saveUserCollection(userId, bookId, (byte) 2);
        
        return buildSuccessResponse("加入书架成功", null);
    }

    @GetMapping("/reading-time/{bookId}")
    public ResponseEntity<Map<String, Object>> getReadingTime(@PathVariable Long bookId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return buildUnauthorizedResponse();
        }
        UserBehavior ub = userBehaviorRepository.findByUserIdAndBookId(userId, bookId);
        double seconds = 0.0;
        if (ub != null && UserBehavior.VIEW.equals(ub.getBehaviorType()) && ub.getScore() != null) {
            seconds = ub.getScore();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("seconds", seconds);
        return buildSuccessResponse("获取阅读时长成功", data);
    }

    @PostMapping("/reading-time")
    @Transactional
    public ResponseEntity<Map<String, Object>> addReadingTime(@RequestBody Map<String, Object> payload) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return buildUnauthorizedResponse();
        }
        Object bookIdVal = payload.get("bookId");
        Object secVal = payload.get("deltaSeconds");
        if (!(bookIdVal instanceof Number) || !(secVal instanceof Number)) {
            return buildErrorResponse("参数错误");
        }
        Long bookId = ((Number) bookIdVal).longValue();
        double deltaSeconds = ((Number) secVal).doubleValue();
        Long categoryId = null;
        Object catVal = payload.get("categoryId");
        if (catVal instanceof Number) {
            categoryId = ((Number) catVal).longValue();
        } else {
            Book b = bookService.getBookById(bookId);
            categoryId = b != null ? b.getCategoryId() : null;
        }
        UserBehavior ub = userBehaviorRepository.findByUserIdAndBookId(userId, bookId);
        if (ub == null) {
            ub = new UserBehavior();
            ub.setUserId(userId);
            ub.setBookId(bookId);
            ub.setCategoryId(categoryId);
            ub.setBehaviorType(UserBehavior.VIEW);
            ub.setScore(Math.max(0.0, deltaSeconds));
            ub.setCreateTime(LocalDateTime.now());
        } else {
            if (ub.getScore() == null) ub.setScore(0.0);
            ub.setScore(ub.getScore() + Math.max(0.0, deltaSeconds));
            ub.setBehaviorType(UserBehavior.VIEW);
            ub.setCreateTime(LocalDateTime.now());
            if (ub.getCategoryId() == null) ub.setCategoryId(categoryId);
        }
        userBehaviorRepository.save(ub);
        return buildSuccessResponse("累计阅读时长成功", null);
    }
    private void saveUserCollection(Long userId, Long bookId, byte type) {
        UserCollection existingCollection = userCollectionRepository.findByUserIdAndBookIdAndCollectionType(
            userId, bookId, type
        );
        
        if (existingCollection == null) {
            UserCollection userCollection = new UserCollection(
                userId,
                bookId,
                type,
                null
            );
            userCollectionRepository.save(userCollection);
        }
    }
    
    /**
     * 记录用户评分行为 (RATE)
     */
    @PostMapping("/rate")
    @Transactional
    public ResponseEntity<Map<String, Object>> recordRateBehavior(@RequestBody UserBehaviorRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return buildUnauthorizedResponse();
        }
        Long bookId = request.getBookId();
        Long categoryId = request.getCategoryId();
        
        Double rateObj = request.getRate();
        Integer ratingObj = request.getRating();
        double ratingValue;
        
        if (rateObj != null) {
            ratingValue = rateObj;
        } else if (ratingObj != null) {
            ratingValue = ratingObj;
        } else {
            ratingValue = 3.0;
        }

        // 1. 记录用户行为到UserBehavior表 (RATE, score=用户评分)
        bookRecommendService.recordUserBehavior(
            userId,
            bookId,
            categoryId,
            UserBehavior.RATE,
            ratingValue
        );
        
        // 2. 更新图书统计信息
        BookStats bookStats = bookStatsRepository.findByBookId(bookId);
        if (bookStats != null) {
            bookStats.setRatingCount(bookStats.getRatingCount() + 1);
            bookStats.setRatingSum(bookStats.getRatingSum() + (int)Math.round(ratingValue));
            bookStats.setUpdatedAt(LocalDateTime.now());
            bookStatsRepository.save(bookStats);
        }
        
        // 3. 保存或更新用户评分记录到Rating表
        Rating existingRating = ratingRepository.findByUserIdAndBookId(userId, bookId);
        java.math.BigDecimal scoreDecimal = new java.math.BigDecimal(ratingValue);
        
        if (existingRating != null) {
            existingRating.setScore(scoreDecimal);
            ratingRepository.save(existingRating);
        } else {
            Rating newRating = new Rating(
                userId,
                bookId,
                scoreDecimal,
                null,
                (byte) 1
            );
            ratingRepository.save(newRating);
        }
        
        return buildSuccessResponse("记录评分成功", null);
    }
    
    /**
     * 记录用户评论行为 (REVIEW)
     */
    @PostMapping("/comment")
    @Transactional
    public ResponseEntity<Map<String, Object>> recordCommentBehavior(@RequestBody BookReviewRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return buildUnauthorizedResponse();
        }
        Long bookId = request.getBookId();
        Long categoryId = request.getCategoryId();
        double weight = classifyReviewWeight(request.getContent());
        bookRecommendService.recordUserBehavior(userId, bookId, categoryId, UserBehavior.REVIEW, weight);
        
        // 2. 更新图书统计信息
        updateBookViewStats(bookId);
        
        // 3. 保存书评到Review表
        Review review = new Review();
        review.setUserId(userId);
        review.setBookId(bookId);
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());
        review.setStatus((byte) 1);
        reviewRepository.save(review);
        
        return buildSuccessResponse("记录评论成功", null);
    }

    private double classifyReviewWeight(String content) {
        if (content == null) return 0.0;
        String text = content.toLowerCase();
        String[] tokens = text.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\u4e00-\\u9fff]+", " ").trim().split("\\s+");
        if (tokens.length == 0) return 0.0;
        String[] posWords = new String[]{"好看","精彩","喜欢","满意","推荐","佳作","优秀","出色","感人","震撼","不错","很棒","精彩绝伦","受益","值得","精彩纷呈","赞","great","good","excellent","amazing","love"};
        String[] negWords = new String[]{"差","不好","失望","糟糕","一般","平庸","后悔","无聊","枯燥","垃圾","难看","糟","烂","不推荐","差劲","bad","terrible","awful","boring","disappoint"};
        java.util.Map<String, Double> pos = new java.util.HashMap<>();
        java.util.Map<String, Double> neg = new java.util.HashMap<>();
        for (String w : posWords) pos.put(w, 1.0);
        for (String w : negWords) neg.put(w, 1.0);
        java.util.Map<String, Integer> tf = new java.util.HashMap<>();
        for (String t : tokens) {
            if (t.isEmpty()) continue;
            tf.put(t, tf.getOrDefault(t, 0) + 1);
        }
        double posScore = Math.log(0.5);
        double negScore = Math.log(0.5);
        for (java.util.Map.Entry<String, Integer> e : tf.entrySet()) {
            String t = e.getKey();
            int f = e.getValue();
            double idfPos = pos.containsKey(t) ? 1.5 : 1.0;
            double idfNeg = neg.containsKey(t) ? 1.5 : 1.0;
            double tfw = Math.log(1 + f);
            posScore += Math.log(1e-3 + (pos.containsKey(t) ? 0.9 : 0.1)) * tfw * idfPos;
            negScore += Math.log(1e-3 + (neg.containsKey(t) ? 0.9 : 0.1)) * tfw * idfNeg;
        }
        if (posScore - negScore > Math.log(1.2)) return 4.0;
        if (negScore - posScore > Math.log(1.2)) return 0.0;
        return 2.0;
    }

    private void updateBookViewStats(Long bookId) {
        BookStats bookStats = bookStatsRepository.findByBookId(bookId);
        if (bookStats != null) {
            bookStats.setViewCount(bookStats.getViewCount() + 1);
            bookStats.setUpdatedAt(LocalDateTime.now());
            bookStatsRepository.save(bookStats);
        }
    }

    /**
     * 获取用户分类偏好
     * <p>
     * 获取当前登录用户的分类偏好权重
     * </p>
     *
     * @return 分类ID到权重的映射
     */
    @GetMapping("/preferences")
    public ResponseEntity<Map<String, Object>> getUserPreferences() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return buildUnauthorizedResponse();
        }
        Map<Long, Double> preferences = bookRecommendService.getUserCategoryPreferences(userId);
        return buildSuccessResponse("获取偏好成功", preferences);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendations(@RequestParam(defaultValue = "10") int limit) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return buildUnauthorizedResponse();
        }
        List<Long> recommendedBooks = bookRecommendService.getRecommendedBooks(userId, limit);
        return buildSuccessResponse("获取推荐成功", recommendedBooks);
    }

    /**
     * 获取用户个性化热门搜索关键词（基于推荐TopK的前N本书名）
     */
    @GetMapping("/hot-searches")
    public ResponseEntity<Map<String, Object>> getHotSearches(@RequestParam(defaultValue = "8") int limit) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return buildUnauthorizedResponse();
        }
        List<Long> recommendedIds = bookRecommendService.getRecommendedBooks(userId, limit);
        List<String> titles = new java.util.ArrayList<>();
        for (Long id : recommendedIds) {
            var book = bookService.getBookById(id);
            if (book != null && book.getTitle() != null && !book.getTitle().isEmpty()) {
                titles.add(book.getTitle());
            }
            if (titles.size() >= limit) break;
        }
        return buildSuccessResponse("获取热门搜索成功", titles);
    }

    /**
     * 获取当前登录用户对某本书的个人评分
     */
    @GetMapping("/my-rate/{bookId}")
    public ResponseEntity<Map<String, Object>> getMyRate(@PathVariable Long bookId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return buildUnauthorizedResponse();
        }
        Rating existing = ratingRepository.findByUserIdAndBookId(userId, bookId);
        Map<String, Object> data = new HashMap<>();
        if (existing != null && existing.getScore() != null) {
            data.put("rating", existing.getScore().doubleValue());
        } else {
            data.put("rating", null);
        }
        return buildSuccessResponse("获取个人评分成功", data);
    }

    /**
     * 同步所有历史数据到 user_behavior (管理接口)
     */
    @PostMapping("/sync-all")
    public ResponseEntity<Map<String, Object>> syncAllBehaviors() {
        int count = bookRecommendService.syncAllBehaviors();
        return buildSuccessResponse("同步完成，共更新/插入 " + count + " 条行为数据", count);
    }

    /**
     * 清理所有 user_behavior 数据 (管理接口)
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, Object>> clearAllBehaviors() {
        bookRecommendService.clearAllBehaviors();
        return buildSuccessResponse("清理完成", null);
    }

    private ResponseEntity<Map<String, Object>> buildUnauthorizedResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(401, "用户未登录"));
        response.put("data", null);
        return ResponseEntity.status(401).body(response);
    }

    private ResponseEntity<Map<String, Object>> buildSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, message));
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(400, message));
        response.put("data", null);
        return ResponseEntity.status(400).body(response);
    }

    private Map<String, Object> buildMeta(int code, String message) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("code", code);
        meta.put("message", message);
        return meta;
    }
}
