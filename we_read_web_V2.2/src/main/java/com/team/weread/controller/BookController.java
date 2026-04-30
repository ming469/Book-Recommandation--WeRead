package com.team.weread.controller;

import com.team.weread.dto.BookDTO;
import com.team.weread.model.*;
import com.team.weread.dto.AdminBookRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import com.team.weread.service.AdminService;
import com.team.weread.service.BookRecommendService;
import com.team.weread.service.BookService;
import com.team.weread.repository.ReviewRepository;
import com.team.weread.repository.CategoryRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 图书相关接口控制器
 * <p>
 * 提供图书查询、搜索、分类浏览等功能的REST API
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private BookRecommendService bookRecommendService;

    @Autowired
    private com.team.weread.service.UserService userService;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 构造函数注入BookService
     */
    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

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
     * 获取图书列表（分页）
     *
     * @param page      页码，默认为0
     * @param size      每页大小，默认为10
     * @param sortBy    排序字段，默认为bookID
     * @param direction 排序方向，默认为升序(asc)
     * @return 包含图书列表和分页信息的响应
     */
    @GetMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "bookID") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Book> books = bookService.getAllBooks(pageable);

        Map<String, Object> pageData = createPageResponse(books);

        Map<String, Object> response = new HashMap<>();
        response.put("meta", Map.of(
            "code", 200,
            "message", "获取图书列表成功"
        ));
        response.put("data", pageData);
        return ResponseEntity.ok(response);
    }

    /**
     * 按分类分页获取图书
     * 支持按分类ID或分类名称查询，优先使用分类ID
     */
    @GetMapping("/by-category")
    @Transactional
    public ResponseEntity<Map<String, Object>> getBooksByCategory(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long resolvedId = categoryId;
        if (resolvedId == null) {
            if (category == null || category.isBlank()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "缺少分类参数");
            }
            Category c = categoryRepository.findByName(category);
            if (c == null) {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "分类不存在");
            }
            resolvedId = c.getCategoryId();
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookService.getBooksByCategory(String.valueOf(resolvedId), pageable);
        Map<String, Object> pageData = createPageResponse(books);
        Map<String, Object> response = new HashMap<>();
        response.put("meta", Map.of("code", 200, "message", "获取分类图书成功"));
        response.put("data", pageData);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取图书评论（简单分页）
     */
    @GetMapping("/{id}/comments")
    @Transactional
    public ResponseEntity<Map<String, Object>> getBookComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (id == null || id <= 0) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "图书ID必须为正整数");
        }
        List<Review> all = reviewRepository.findByBookId(id);
        int total = all.size();
        int pageIndex = Math.max(0, page - 1);
        int from = Math.min(pageIndex * size, total);
        int to = Math.min(from + size, total);
        List<Review> list = all.subList(from, to);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Review r : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getReviewId());
            m.put("userId", r.getUserId());
            m.put("title", r.getTitle());
            m.put("content", r.getContent());
            m.put("createdAt", r.getCreatedAt());
            items.add(m);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", items);
        data.put("total", total);
        data.put("currentPage", page);
        data.put("size", size);
        data.put("totalPages", (int) Math.ceil(total / (double) size));
        Map<String, Object> response = new HashMap<>();
        response.put("meta", Map.of("code", 200, "message", "获取评论成功"));
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取图书详情
     *
     * @param id 图书ID
     * @return 图书详情响应
     */
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> getBookById(@PathVariable Long id) {
        // 参数校验
        if (id == null || id <= 0) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "图书ID必须为正整数");
        }

        try {
            Book book = bookService.getBookById(id);
            if (book == null) {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "图书不存在或已被删除");
            }

            BookDTO bookData = toBookDto(book);

            Map<String, Object> response = new HashMap<>();
            response.put("meta", Map.of(
                "code", HttpStatus.OK.value(),
                "message", "获取图书详情成功"
            ));
            response.put("data", bookData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");
        }
    }

    /**
     * 搜索图书
     *
     * @param keyword 搜索关键词
     * @param page    页码，默认为0
     * @param size    每页大小，默认为10
     * @return 搜索结果响应
     */
    @GetMapping("/search")
    @Transactional
    public ResponseEntity<Map<String, Object>> searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size);
        Page<Book> books = bookService.searchBooks(keyword, pageable);

        Map<String, Object> pageData = createPageResponse(books);

        Map<String, Object> response = new HashMap<>();
        response.put("meta", Map.of(
            "code", 200,
            "message", "搜索图书成功"
        ));
        response.put("data", pageData);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取推荐图书
     *
     * @param page 页码，默认为0
     * @param size 每页大小，默认为10
     * @return 推荐图书列表响应
     */
    @GetMapping("/recommended")
    @Transactional
    public ResponseEntity<Map<String, Object>> getRecommendedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = getCurrentUserId();
        Page<Book> books;

        if (userId != null) {
            // 已登录用户，使用个性化推荐逻辑 (UserBehavior驱动)
            List<Long> recommendedIds = bookRecommendService.getRecommendedBooks(userId, (page + 1) * size);
            
            // 手动实现分页，从推荐列表中提取对应页的内容
            int start = page * size;
            if (start < recommendedIds.size()) {
                int end = Math.min(start + size, recommendedIds.size());
                List<Long> pageIds = recommendedIds.subList(start, end);
                
                List<Book> bookList = new ArrayList<>();
                for (Long bookId : pageIds) {
                    Book book = bookService.getBookById(bookId);
                    if (book != null) {
                        bookList.add(book);
                    }
                }
                
                books = new PageImpl<>(bookList, PageRequest.of(page, size), recommendedIds.size());
            } else {
                books = new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), recommendedIds.size());
            }
        } else {
            // 未登录用户，回退到全局推荐 (根据评分)
            Pageable pageable = PageRequest.of(page, size);
            books = bookService.getRecommendedBooks(pageable);
        }

        Map<String, Object> pageData = createPageResponse(books);

        Map<String, Object> response = new HashMap<>();
        response.put("meta", Map.of(
            "code", 200,
            "message", userId != null ? "获取个性化推荐图书成功" : "获取热门推荐图书成功"
        ));
        response.put("data", pageData);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取最新图书
     *
     * @param page 页码，默认为0
     * @param size 每页大小，默认为10
     * @return 最新图书列表响应
     */
    @GetMapping("/latest")
    @Transactional
    public ResponseEntity<Map<String, Object>> getLatestBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookService.getLatestBooks(pageable);

        Map<String, Object> pageData = createPageResponse(books);

        Map<String, Object> response = new HashMap<>();
        response.put("meta", Map.of(
            "code", 200,
            "message", "获取最新图书成功"
        ));
        response.put("data", pageData);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取最受欢迎图书
     */
    @GetMapping("/popular")
    @Transactional
    public ResponseEntity<Map<String, Object>> getPopularBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookService.getPopularBooks(pageable);

        Map<String, Object> pageData = createPageResponse(books);

        Map<String, Object> response = new HashMap<>();
        response.put("meta", Map.of(
            "code", 200,
            "message", "获取热门图书成功"
        ));
        response.put("data", pageData);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取收藏最多图书
     */
    @GetMapping("/collected")
    @Transactional
    public ResponseEntity<Map<String, Object>> getMostCollectedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookService.getMostCollectedBooks(pageable);

        Map<String, Object> pageData = createPageResponse(books);

        Map<String, Object> response = new HashMap<>();
        response.put("meta", Map.of(
            "code", 200,
            "message", "获取收藏榜图书成功"
        ));
        response.put("data", pageData);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据分类获取图书
     *
     * @param category 图书分类
     * @param page     页码，默认为0
     * @param size     每页大小，默认为10
     * @return 分类图书列表响应
     */
    @GetMapping("/category/{category}")
    @Transactional
    public ResponseEntity<Map<String, Object>> getBooksByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookService.getBooksByCategory(category, pageable);

        Map<String, Object> pageData = createPageResponse(books);

        Map<String, Object> response = new HashMap<>();
        response.put("meta", Map.of(
            "code", 200,
            "message", "获取分类图书成功"
        ));
        response.put("data", pageData);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取某本图书的相似图书（基于 IUF-ItemCF 相似度）
     */
    @GetMapping("/{id}/related")
    @Transactional
    public ResponseEntity<Map<String, Object>> getRelatedBooks(
            @PathVariable Long id,
            @RequestParam(defaultValue = "6") int limit) {
        if (id == null || id <= 0) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "图书ID必须为正整数");
        }
        try {
            String key = "item_sim:" + id;
            Map<Object, Object> simMap = readRedisHashEntries(key);

            List<Long> ids = new ArrayList<>();
            if (simMap != null && !simMap.isEmpty()) {
                simMap.entrySet().stream()
                        .map(e -> new Object[] { e.getKey().toString(), e.getValue().toString() })
                        .map(arr -> {
                            try {
                                return new Object[] { Long.valueOf(arr[0].toString()),
                                        Double.parseDouble(arr[1].toString()) };
                            } catch (Exception ex) {
                                return null;
                            }
                        })
                        .filter(o -> o != null)
                        .sorted((a, b) -> Double.compare((Double) b[1], (Double) a[1]))
                        .limit(limit)
                        .forEach(o -> ids.add((Long) o[0]));
            }
            List<BookDTO> list = new ArrayList<>();
            if (!ids.isEmpty()) {
                for (Long bid : ids) {
                    if (bid.equals(id)) continue;
                    Book b = bookService.getBookById(bid);
                    if (b != null) {
                        list.add(toBookDto(b));
                    }
                    if (list.size() >= limit) break;
                }
            } else {
                // Redis或相似度为空时的兜底：同分类下按评分/热度取相邻图书
                Book current = bookService.getBookById(id);
                if (current == null) {
                    return buildErrorResponse(HttpStatus.NOT_FOUND, "图书不存在");
                }
                Page<Book> page = bookService.getBooksByCategory(
                        String.valueOf(current.getCategoryId()),
                        PageRequest.of(0, limit + 5)
                );
                for (Book b : page.getContent()) {
                    if (b.getBookId().equals(id)) continue;
                    list.add(toBookDto(b));
                    if (list.size() >= limit) break;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("meta", Map.of("code", 200, "message", "获取相似图书成功"));
            response.put("data", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取相似图书失败");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object> readRedisHashEntries(String key) {
        try {
            if (stringRedisTemplate == null) return null;
            Map<Object, Object> res = stringRedisTemplate.opsForHash().entries(key);
            return res;
        } catch (Exception ignored) {
            return null;
        }
    }
    /**
     * 创建分页响应数据
     *
     * @param page 分页对象
     * @return 包含分页信息的Map
     */
    private Map<String, Object> createPageResponse(Page<Book> page) {
        var bookList = page.getContent().stream()
                .map(this::toBookDto)
                .toList();

        return Map.of(
                "content", bookList,
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "currentPage", page.getNumber(),
                "size", page.getSize()
        );
    }

    private BookDTO toBookDto(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getBookId());
        dto.setTitle(book.getTitle());
        dto.setIsbn(book.getIsbn());
        dto.setAuthor(book.getAuthor());
        dto.setCategoryId(book.getCategoryId());
        dto.setCategoryName(book.getCategory() != null ? book.getCategory().getName() : "");
        dto.setPublisher(book.getPublisher());
        dto.setStatus(book.getStatus());
        dto.setHasEbook(book.isHasEbook());
        dto.setPublicationDate(book.getPublicationDate());
        Integer publishYear = book.getPublishYear();
        if (publishYear == null && book.getPublicationDate() != null) {
            publishYear = book.getPublicationDate().getYear();
        }
        dto.setPublishYear(publishYear);
        dto.setPages(book.getPages());
        dto.setLanguage(book.getLanguage());
        dto.setDescription(book.getDescription());
        dto.setViewCount(book.getViewCount());
        dto.setCollectionCount(book.getCollectionCount());
        dto.setRatingSum(book.getRating());
        dto.setRatingCount(book.getRatingCount());
        dto.setAvgRating(book.getAvgRating());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        dto.setRecommended(book.isRecommended());
        dto.setLocation(book.getLocation());
        dto.setCallNumber(book.getCallNumber());
        dto.setStock(book.getStock());
        return dto;
    }




    /**
     * 构建错误响应
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("meta", Map.of(
            "code", status.value(),
            "message", message
        ));
        response.put("data", null);
        return ResponseEntity.status(status).body(response);
    }
}
