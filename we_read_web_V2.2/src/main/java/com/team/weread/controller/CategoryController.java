package com.team.weread.controller;

import com.team.weread.model.Category;
import com.team.weread.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书分类控制器
 * <p>
 * 处理图书分类相关的请求
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 获取图书分类列表
     * <p>
     * 返回树形结构的分类数据
     * </p>
     *
     * @return 分类列表响应
     */
    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCategoryList() {
        List<Category> categories = categoryService.getCategoryTree();

        Map<String, Object> meta = new HashMap<>();
        meta.put("code", 200);
        meta.put("message", "获取图书分类成功");

        Map<String, Object> response = new HashMap<>();
        response.put("meta", meta);
        response.put("data", categories);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getPublicCategoryList() {
        List<Category> categories = categoryService.getCategoryTree();

        Map<String, Object> meta = new HashMap<>();
        meta.put("code", 200);
        meta.put("message", "获取图书分类成功");

        Map<String, Object> response = new HashMap<>();
        response.put("meta", meta);
        response.put("data", categories);

        return ResponseEntity.ok(response);
    }
}
