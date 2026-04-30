package com.team.weread.service;

import com.team.weread.model.Category;
import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 获取分类树形结构
     */
    List<Category> getCategoryTree();
}
