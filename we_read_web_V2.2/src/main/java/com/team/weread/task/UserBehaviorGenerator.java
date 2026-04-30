package com.team.weread.task;

import com.team.weread.model.UserBehavior;
import com.team.weread.model.Book;
import com.team.weread.model.Category;
import com.team.weread.model.User;
import com.team.weread.repository.BookRepository;
import com.team.weread.repository.CategoryRepository;
import com.team.weread.repository.UserRepository;
import com.team.weread.service.BookRecommendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 用户行为数据生成器
 * <p>
 * 在系统启动时生成模拟的用户行为数据，用于推荐系统。
 * 确保：
 * 1. 同一个分类的书会被相似用户喜欢
 * 2. 不同分类之间有区分
 * 3. 每个用户交互 60-100本书
 * 4. 行为类型分布：READ (60%, score=3), COLLECT (25%, score=4), RATE (15%, score=3-5)
 * 5. 用户兴趣偏好：70% 来自主兴趣分类，30% 来自其他分类
 * </p>
 */
@Component
public class UserBehaviorGenerator implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(UserBehaviorGenerator.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRecommendService bookRecommendService;

    // 行为类型及其分布
    private static final Map<String, Double> BEHAVIOR_DISTRIBUTION = new LinkedHashMap<>();
    static {
        BEHAVIOR_DISTRIBUTION.put(UserBehavior.VIEW, 0.60); // READ 对应 VIEW
        BEHAVIOR_DISTRIBUTION.put(UserBehavior.COLLECT, 0.25);
        BEHAVIOR_DISTRIBUTION.put(UserBehavior.RATE, 0.15);
    }

    private static final int MIN_INTERACTIONS = 30;
    private static final int MAX_INTERACTIONS = 50;
    private static final double MAIN_CATEGORY_RATIO = 0.7;

    @Override
    @Transactional
    public void run(String... args) {
        if (bookRecommendService.countAllBehaviors() > 5000) {
            logger.info("用户行为数据已就绪，跳过自动生成。");
            return;
        }
        logger.info("开始生成模拟用户行为数据...");

        List<User> users = userRepository.findAll();
        List<Category> allCategories = categoryRepository.findAll();
        List<Book> allBooks = bookRepository.findAll();

        if (users.isEmpty() || allCategories.isEmpty() || allBooks.isEmpty()) {
            logger.warn("用户、分类或图书数据为空，无法生成用户行为数据。");
            return;
        }

        // 过滤掉没有图书的分类，并构建分类ID到图书列表的映射
        Map<Long, List<Book>> categoryBooksMap = allBooks.stream()
                .filter(book -> book.getCategoryId() != null)
                .collect(Collectors.groupingBy(Book::getCategoryId));

        List<Category> availableCategories = allCategories.stream()
                .filter(category -> categoryBooksMap.containsKey(category.getCategoryId()) && !categoryBooksMap.get(category.getCategoryId()).isEmpty())
                .collect(Collectors.toList());

        if (availableCategories.isEmpty()) {
            logger.warn("没有可用的分类图书数据，无法生成用户行为数据。");
            return;
        }

        int totalBehaviorsGenerated = 0;

        for (User user : users) {
            // 随机分配一个主兴趣分类
            Category mainCategory = availableCategories.get(ThreadLocalRandom.current().nextInt(availableCategories.size()));
            List<Book> mainCategoryBooks = categoryBooksMap.getOrDefault(mainCategory.getCategoryId(), Collections.emptyList());

            // 确定用户交互的图书数量
            int numInteractions = ThreadLocalRandom.current().nextInt(MIN_INTERACTIONS, MAX_INTERACTIONS + 1);
            int mainCategoryInteractionCount = (int) (numInteractions * MAIN_CATEGORY_RATIO);
            int otherCategoryInteractionCount = numInteractions - mainCategoryInteractionCount;

            Set<Long> interactedBookIds = new HashSet<>(); // 记录已交互的图书ID，避免重复

            // 1. 从主兴趣分类中选择图书
            List<Book> selectedMainBooks = selectRandomBooks(mainCategoryBooks, mainCategoryInteractionCount, interactedBookIds);
            recordBehaviorsForUser(user, selectedMainBooks, interactedBookIds);
            totalBehaviorsGenerated += selectedMainBooks.size();

            // 2. 从其他分类中选择图书
            List<Category> otherCategories = availableCategories.stream()
                    .filter(c -> !c.getCategoryId().equals(mainCategory.getCategoryId()))
                    .collect(Collectors.toList());

            List<Book> selectedOtherBooks = new ArrayList<>();
            if (!otherCategories.isEmpty()) {
                // 尝试从多个其他分类中选择图书，直到达到数量或遍历完所有其他分类
                List<Book> allOtherBooks = otherCategories.stream()
                        .flatMap(c -> categoryBooksMap.getOrDefault(c.getCategoryId(), Collections.emptyList()).stream())
                        .collect(Collectors.toList());
                selectedOtherBooks = selectRandomBooks(allOtherBooks, otherCategoryInteractionCount, interactedBookIds);
                recordBehaviorsForUser(user, selectedOtherBooks, interactedBookIds);
                totalBehaviorsGenerated += selectedOtherBooks.size();
            }

            logger.info("用户 {} (ID: {}) 交互了 {} 本书 (主分类: {})", user.getUsername(), user.getId(), interactedBookIds.size(), mainCategory.getName());
        }

        logger.info("模拟用户行为数据生成完成。总共生成 {} 条行为记录。", totalBehaviorsGenerated);

        // 触发相似度计算
        logger.info("开始计算物品相似度...");
        bookRecommendService.calculateAndStoreSimilarities();
        logger.info("物品相似度计算完成并已存入 Redis。");
    }

    /**
     * 从给定图书列表中随机选择指定数量的图书，并确保不重复
     */
    private List<Book> selectRandomBooks(List<Book> availableBooks, int count, Set<Long> interactedBookIds) {
        List<Book> selectedBooks = new ArrayList<>();
        List<Book> shuffledBooks = new ArrayList<>(availableBooks);
        Collections.shuffle(shuffledBooks); // 随机打乱

        for (Book book : shuffledBooks) {
            if (selectedBooks.size() >= count) {
                break;
            }
            if (!interactedBookIds.contains(book.getBookId())) {
                selectedBooks.add(book);
                interactedBookIds.add(book.getBookId());
            }
        }
        return selectedBooks;
    }

    /**
     * 为用户记录行为
     */
    private void recordBehaviorsForUser(User user, List<Book> books, Set<Long> interactedBookIds) {
        for (Book book : books) {
            String behaviorType = getRandomBehaviorType();
            Double score = getScoreForBehaviorType(behaviorType);
            
            // 记录用户行为
            bookRecommendService.recordUserBehavior(user.getId(), book.getBookId(), book.getCategoryId(), behaviorType, score);
        }
    }

    /**
     * 根据预设分布随机获取行为类型
     */
    private String getRandomBehaviorType() {
        double rand = ThreadLocalRandom.current().nextDouble();
        double cumulativeProbability = 0.0;
        for (Map.Entry<String, Double> entry : BEHAVIOR_DISTRIBUTION.entrySet()) {
            cumulativeProbability += entry.getValue();
            if (rand <= cumulativeProbability) {
                return entry.getKey();
            }
        }
        return UserBehavior.VIEW; // 默认返回VIEW
    }

    /**
     * 根据行为类型获取分数
     */
    private Double getScoreForBehaviorType(String behaviorType) {
        switch (behaviorType) {
            case UserBehavior.VIEW:
                return 3.0; // READ 对应 VIEW, score=3
            case UserBehavior.COLLECT:
                return 4.0;
            case UserBehavior.RATE:
                return ThreadLocalRandom.current().nextDouble(3.0, 5.1); // 3-5随机分
            default:
                return 1.0; // 默认值
        }
    }
}
