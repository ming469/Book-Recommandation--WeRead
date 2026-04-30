package com.team.weread.controller;

import com.team.weread.repository.*;
import com.team.weread.task.DataInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookStatsRepository bookStatsRepository;

    @Autowired
    private BrowsingHistoryRepository browsingHistoryRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserBehaviorRepository userBehaviorRepository;

    @Autowired
    private UserCollectionRepository userCollectionRepository;

    @Autowired
    private DataInitializer dataInitializer;

    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, String>> cleanup() {
        // 1. 删除用户行为相关数据（外键引用 Book）
        browsingHistoryRepository.deleteAll();
        ratingRepository.deleteAll();
        reviewRepository.deleteAll();
        userBehaviorRepository.deleteAll();
        userCollectionRepository.deleteAll();

        // 2. 删除图书关联数据
        bookStatsRepository.deleteAll();
        
        // 3. 删除主表数据
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        
        return ResponseEntity.ok(Map.of("message", "数据库已彻底清空"));
    }

    @PostMapping("/init-data")
    public ResponseEntity<Map<String, String>> initData() {
        dataInitializer.run();
        return ResponseEntity.ok(Map.of("message", "数据初始化已触发"));
    }
}
