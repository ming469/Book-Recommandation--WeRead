package com.team.weread.controller;

import com.team.weread.dto.ChangePasswordRequest;
import com.team.weread.model.UserBehavior;
import com.team.weread.model.Book;
import com.team.weread.model.BrowsingHistory;
import com.team.weread.model.User;
import com.team.weread.model.UserCollection;
import com.team.weread.repository.BrowsingHistoryRepository;
import com.team.weread.repository.UserCollectionRepository;
import com.team.weread.service.BookRecommendService;
import com.team.weread.service.BookService;
import com.team.weread.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final BrowsingHistoryRepository browsingHistoryRepository;
    private final UserCollectionRepository userCollectionRepository;
    
    @Autowired
    private BookRecommendService bookRecommendService;

    @Autowired
    private BookService bookService;

    @Autowired
    public UserController(UserService userService, BrowsingHistoryRepository browsingHistoryRepository, UserCollectionRepository userCollectionRepository) {
        this.userService = userService;
        this.browsingHistoryRepository = browsingHistoryRepository;
        this.userCollectionRepository = userCollectionRepository;
    }

    /**
     * 修改密码接口
     *
     * @param request 修改密码请求参数
     * @return 修改结果
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody ChangePasswordRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(401, "用户未登录"));
            response.put("data", null);
            return ResponseEntity.status(401).body(response);
        }

        if (request.getAccount() == null || request.getAccount().isEmpty()) {
            request.setAccount(currentUser.getUsername());
        }

        User user = userService.changePassword(request);

        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(401, "原密码不正确"));
            response.put("data", null);

            return ResponseEntity.status(401).body(response);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("user", buildUserData(user));

        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "密码修改成功"));
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getReadHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(401, "用户未登录"));
            response.put("data", null);
            return ResponseEntity.status(401).body(response);
        }

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        Page<BrowsingHistory> historyPage = browsingHistoryRepository.findByUserId(currentUser.getId(), pageable);

        Map<String, Object> data = new HashMap<>();
        data.put("list", historyPage.getContent());
        data.put("total", historyPage.getTotalElements());
        data.put("totalPages", historyPage.getTotalPages());
        data.put("currentPage", historyPage.getNumber() + 1);
        data.put("size", historyPage.getSize());
        data.put("totalBooks", historyPage.getTotalElements());
        data.put("totalPagesRead", historyPage.getTotalElements());
        data.put("readingDays", 0);

        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "获取阅读历史成功"));
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> addReadHistory(@RequestBody Map<String, Object> payload) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(401, "用户未登录"));
            response.put("data", null);
            return ResponseEntity.status(401).body(response);
        }

        Object bookIdValue = payload.get("bookId");
        if (!(bookIdValue instanceof Number)) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(400, "缺少书籍ID"));
            response.put("data", null);
            return ResponseEntity.status(400).body(response);
        }
        Long bookId = ((Number) bookIdValue).longValue();

        // 获取书籍信息以获取categoryId
        Book book = bookService.getBookById(bookId);
        Long categoryId = (book != null) ? book.getCategoryId() : null;

        // 记录用户行为到UserBehavior表 (VIEW, score=1.0)
        bookRecommendService.recordUserBehavior(
            currentUser.getId(),
            bookId,
            categoryId,
            UserBehavior.VIEW,
            UserBehavior.SCORE_VIEW
        );

        BrowsingHistory existing = browsingHistoryRepository.findByUserIdAndBookId(currentUser.getId(), bookId);
        if (existing != null) {
            existing.setViewedAt(LocalDateTime.now());
            browsingHistoryRepository.save(existing);
        } else {
            BrowsingHistory history = new BrowsingHistory();
            history.setUserId(currentUser.getId());
            history.setBookId(bookId);
            history.setSource("manual");
            browsingHistoryRepository.save(history);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "添加阅读历史成功"));
        response.put("data", null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> clearReadHistory() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(401, "用户未登录"));
            response.put("data", null);
            return ResponseEntity.status(401).body(response);
        }

        browsingHistoryRepository.deleteByUserId(currentUser.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "清空阅读历史成功"));
        response.put("data", null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/history/{historyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> deleteReadHistory(@PathVariable Long historyId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(401, "用户未登录"));
            response.put("data", null);
            return ResponseEntity.status(401).body(response);
        }

        Optional<BrowsingHistory> history = browsingHistoryRepository.findById(historyId);
        if (history.isEmpty() || !history.get().getUserId().equals(currentUser.getId())) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(404, "阅读历史不存在"));
            response.put("data", null);
            return ResponseEntity.status(404).body(response);
        }

        browsingHistoryRepository.deleteById(historyId);
        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "删除阅读历史成功"));
        response.put("data", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getFavorites() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(401, "用户未登录"));
            response.put("data", null);
            return ResponseEntity.status(401).body(response);
        }

        List<UserCollection> collections = userCollectionRepository.findByUserIdAndCollectionType(currentUser.getId(), (byte) 3);
        List<Map<String, Object>> list = collections.stream().map(item -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("bookId", item.getBookId());
            entry.put("favoriteTime", item.getCreatedAt());
            return entry;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", list.size());

        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "获取收藏成功"));
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/favorites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> addFavorite(@RequestBody Map<String, Object> payload) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(401, "用户未登录"));
            response.put("data", null);
            return ResponseEntity.status(401).body(response);
        }

        Object bookIdValue = payload.get("bookId");
        if (!(bookIdValue instanceof Number)) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(400, "缺少书籍ID"));
            response.put("data", null);
            return ResponseEntity.status(400).body(response);
        }
        Long bookId = ((Number) bookIdValue).longValue();

        // 获取书籍信息以获取categoryId
        Book book = bookService.getBookById(bookId);
        Long categoryId = (book != null) ? book.getCategoryId() : null;

        // 记录用户行为到UserBehavior表 (COLLECT, score=3.0)
        bookRecommendService.recordUserBehavior(
            currentUser.getId(),
            bookId,
            categoryId,
            UserBehavior.COLLECT,
            UserBehavior.SCORE_COLLECT
        );

        UserCollection existing = userCollectionRepository.findByUserIdAndBookIdAndCollectionType(currentUser.getId(), bookId, (byte) 3);
        if (existing == null) {
            UserCollection collection = new UserCollection(currentUser.getId(), bookId, (byte) 3, null);
            userCollectionRepository.save(collection);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "添加收藏成功"));
        response.put("data", null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/favorites/{bookId}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<Map<String, Object>> removeFavorite(@PathVariable Long bookId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(401, "用户未登录"));
            response.put("data", null);
            return ResponseEntity.status(401).body(response);
        }

        userCollectionRepository.deleteByUserIdAndBookIdAndCollectionType(currentUser.getId(), bookId, (byte) 3);
        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "取消收藏成功"));
        response.put("data", null);
        return ResponseEntity.ok(response);
    }

    /**
     * 分页获取用户书架
     */
    @GetMapping("/shelf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getShelf(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(401, "用户未登录"));
            response.put("data", null);
            return ResponseEntity.status(401).body(response);
        }
        List<UserCollection> all = userCollectionRepository.findByUserIdAndCollectionType(currentUser.getId(), (byte) 2);
        int total = all.size();
        int pageIndex = Math.max(0, page - 1);
        int from = Math.min(pageIndex * size, total);
        int to = Math.min(from + size, total);
        List<UserCollection> pageList = all.subList(from, to);
        List<Map<String, Object>> content = pageList.stream().map(item -> {
            Map<String, Object> m = new HashMap<>();
            m.put("bookId", item.getBookId());
            m.put("addedAt", item.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("currentPage", page);
        data.put("size", size);
        data.put("totalItems", total);
        data.put("totalPages", (int) Math.ceil(total / (double) size));
        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "获取书架成功"));
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * 书架移除单本书
     */
    @DeleteMapping("/shelf/{bookId}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<Map<String, Object>> removeFromShelf(@PathVariable Long bookId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(401, "用户未登录"));
            response.put("data", null);
            return ResponseEntity.status(401).body(response);
        }
        try {
            UserCollection existing = userCollectionRepository.findByUserIdAndBookIdAndCollectionType(
                    currentUser.getId(), bookId, (byte) 2);
            if (existing == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("meta", buildMeta(404, "该图书不在书架中"));
                response.put("data", null);
                return ResponseEntity.status(404).body(response);
            }
            userCollectionRepository.delete(existing);
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(200, "已移出书架"));
            response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(500, "移出书架失败"));
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/favorites/check/{bookId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkFavorite(@PathVariable Long bookId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(401, "用户未登录"));
            response.put("data", null);
            return ResponseEntity.status(401).body(response);
        }

        UserCollection existing = userCollectionRepository.findByUserIdAndBookIdAndCollectionType(currentUser.getId(), bookId, (byte) 3);
        Map<String, Object> data = new HashMap<>();
        data.put("isFavorite", existing != null);

        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "获取收藏状态成功"));
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户个人资料接口
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        User user = getCurrentUser();
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(404, "用户不存在"));
            response.put("data", null);
            return ResponseEntity.status(404).body(response);
        }

        Map<String, Object> userData = buildUserData(user);
        Map<String, Object> data = new HashMap<>();
        data.put("user", userData);

        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "获取用户信息成功"));
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户个人资料接口
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> updateUserProfile(@RequestBody Map<String, Object> payload) {
        User user = getCurrentUser();
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(404, "用户不存在"));
            response.put("data", null);
            return ResponseEntity.status(404).body(response);
        }

        if (payload.containsKey("nickname")) {
            user.setNickname((String) payload.get("nickname"));
        }
        if (payload.containsKey("email")) {
            user.setEmail((String) payload.get("email"));
        }
        if (payload.containsKey("phone")) {
            user.setPhone((String) payload.get("phone"));
        }
        if (payload.containsKey("gender")) {
            Object genderValue = payload.get("gender");
            if (genderValue instanceof Number) {
                user.setGender(((Number) genderValue).intValue());
            }
        }
        if (payload.containsKey("preference")) {
            user.setPreference((String) payload.get("preference"));
        }

        User updatedUser = userService.updateUser(user);

        Map<String, Object> data = new HashMap<>();
        data.put("user", buildUserData(updatedUser));

        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "更新用户信息成功"));
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * 上传用户头像接口
     */
    @PostMapping("/profile/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        User user = getCurrentUser();
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(404, "用户不存在"));
            response.put("data", null);
            return ResponseEntity.status(404).body(response);
        }

        try {
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            String extension = ".img";
            if ("image/png".equalsIgnoreCase(contentType)) {
                extension = ".png";
            } else if ("image/jpeg".equalsIgnoreCase(contentType) || "image/jpg".equalsIgnoreCase(contentType)) {
                extension = ".jpg";
            }

            java.io.File baseDir = new java.io.File("uploads/avatars");
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            java.io.File target = new java.io.File(baseDir, "user-" + user.getId() + extension);
            try (java.io.OutputStream os = new java.io.FileOutputStream(target)) {
                os.write(file.getBytes());
            }

            String avatarPath = "/api/user/profile/avatar/" + user.getId();
            user.setAvatar(avatarPath);
            userService.updateUser(user);

            Map<String, Object> data = new HashMap<>();
            data.put("avatarUrl", avatarPath);

            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(200, "头像更新成功"));
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("meta", buildMeta(500, "头像更新失败"));
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/profile/avatar/{userId}")
    public org.springframework.http.ResponseEntity<byte[]> getAvatar(@PathVariable Long userId) {
        try {
            java.io.File baseDir = new java.io.File("uploads/avatars");
            java.io.File png = new java.io.File(baseDir, "user-" + userId + ".png");
            java.io.File jpg = new java.io.File(baseDir, "user-" + userId + ".jpg");
            java.io.File target = png.exists() ? png : (jpg.exists() ? jpg : null);
            if (target == null || !target.exists()) {
                return org.springframework.http.ResponseEntity.status(404).body(null);
            }
            byte[] bytes = java.nio.file.Files.readAllBytes(target.toPath());
            org.springframework.http.MediaType mediaType = target.getName().endsWith(".png")
                    ? org.springframework.http.MediaType.IMAGE_PNG
                    : org.springframework.http.MediaType.IMAGE_JPEG;
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            return org.springframework.http.ResponseEntity.ok()
                    .headers(headers)
                    .contentType(mediaType)
                    .body(bytes);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(500).body(null);
        }
    }
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return userService.findByUsername(authentication.getName());
    }

    private Map<String, Object> buildMeta(int code, String message) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("code", code);
        meta.put("message", message);
        return meta;
    }

    private Map<String, Object> buildUserData(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("nickname", user.getNickname());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());
        userData.put("avatar", user.getAvatar());
        userData.put("gender", user.getGender());
        userData.put("preference", user.getPreference());
        userData.put("status", user.getStatus());
        return userData;
    }
}
