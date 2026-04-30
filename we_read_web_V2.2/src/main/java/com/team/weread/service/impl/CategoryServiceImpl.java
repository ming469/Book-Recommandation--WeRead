package com.team.weread.service.impl;

import com.team.weread.model.Category;
import com.team.weread.repository.CategoryRepository;
import com.team.weread.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类服务实现类
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findByStatusOrderBySortOrderAsc((byte) 1);
        Map<Long, Category> categoryMap = new HashMap<>();
        List<Category> rootCategories = new ArrayList<>();

        for (Category category : allCategories) {
            categoryMap.put(category.getCategoryId(), category);
        }

        for (Category category : allCategories) {
            Long parentId = category.getParentId();
            if (parentId == null || parentId == 0L) {
                rootCategories.add(category);
            } else {
                Category parent = categoryMap.get(category.getParentId());
                if (parent != null) {
                    parent.getChildren().add(category);
                }
            }
        }
        return rootCategories;
    }
}
