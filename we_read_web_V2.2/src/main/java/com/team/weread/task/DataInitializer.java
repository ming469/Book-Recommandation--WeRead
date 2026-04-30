package com.team.weread.task;

import com.team.weread.model.Category;
import com.team.weread.repository.BookRepository;
import com.team.weread.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 系统初始化数据加载器
 * <p>
 * 在系统启动时检查数据库是否为空，如果为空则预创建分类，等待 Python 爬虫导入豆瓣数据。
 * </p>
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // 定义需要初始化的分类及对应的 Open Library Subject 搜索词
    private static final Map<String, String> CATEGORY_MAP = new LinkedHashMap<>();

    static {
        CATEGORY_MAP.put("文学", "literature");
        CATEGORY_MAP.put("小说", "fiction");
        CATEGORY_MAP.put("历史", "history");
        CATEGORY_MAP.put("科学", "science");
        CATEGORY_MAP.put("艺术", "art");
        CATEGORY_MAP.put("哲学", "philosophy");
        CATEGORY_MAP.put("心理学", "psychology");
        CATEGORY_MAP.put("经济学", "economics");
        CATEGORY_MAP.put("生物", "biology");
        CATEGORY_MAP.put("物理", "physics");
        CATEGORY_MAP.put("化学", "chemistry");
        CATEGORY_MAP.put("计算机", "computer_science");
        CATEGORY_MAP.put("设计", "design");
        CATEGORY_MAP.put("商业", "business");
        CATEGORY_MAP.put("旅游", "travel");
    }

    @Override
    public void run(String... args) {
        try {
            long bookCount = bookRepository.count();
            if (bookCount >= 300) {
                logger.info("数据库中已存在 {} 本图书，冷启动数据已就绪。", bookCount);
                return;
            }

            logger.info("当前图书数量为 {}，等待 Python 爬虫导入豆瓣数据...", bookCount);
            
            // 预创建分类，确保数据库结构完整
            for (String catName : CATEGORY_MAP.keySet()) {
                synchronized (this) {
                    if (categoryRepository.findByName(catName) == null) {
                        Category category = new Category();
                        category.setName(catName);
                        category.setSortOrder(1);
                        category.setStatus((byte) 1);
                        category.setCreatedAt(LocalDateTime.now());
                        category.setUpdatedAt(LocalDateTime.now());
                        categoryRepository.save(category);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("数据初始化检查失败: {}", e.getMessage());
        }
    }
}
