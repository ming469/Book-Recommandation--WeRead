package com.team.weread.service.impl;

import com.team.weread.model.UserBehavior;
import com.team.weread.model.*;
import com.team.weread.repository.*;
import com.team.weread.service.BookRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 图书推荐服务实现类
 */
@Service
public class BookRecommendServiceImpl implements BookRecommendService {

    @Autowired
    private UserBehaviorRepository userBehaviorRepository;

    @Autowired(required = false)
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BrowsingHistoryRepository browsingHistoryRepository;

    @Autowired
    private UserCollectionRepository userCollectionRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Value("${recommend.seedLimit:20}")
    private int seedLimit;

    @Value("${recommend.topK:80}")
    private int topK;

    @Value("${recommend.alpha:0.2}")
    private double alpha;

    @Value("${recommend.categoryCapRatio:0.4}")
    private double categoryCapRatio;


    @Override
    public void recordUserBehavior(Long userId, Long bookId, Long categoryId, String behaviorType, Double score) {
        // 检查是否已经存在该用户对该图书的行为记录
        UserBehavior behavior = userBehaviorRepository.findByUserIdAndBookId(userId, bookId);
        
        if (behavior == null) {
            // 不存在记录，创建新记录
            behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setBookId(bookId);
            behavior.setCategoryId(categoryId);
            behavior.setBehaviorType(behaviorType);
            behavior.setScore(score);
            behavior.setCreateTime(LocalDateTime.now());
        } else {
            // 已存在记录，更新分数（取较大值）和时间
            if (score > behavior.getScore()) {
                behavior.setScore(score);
                behavior.setBehaviorType(behaviorType); // 如果分数更高，通常意味着行为更重要，更新行为类型
            }
            behavior.setCreateTime(LocalDateTime.now());
        }
        
        userBehaviorRepository.save(behavior);
    }

    @Async
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    public void calculateAndStoreSimilarities() {
        long startTime = System.currentTimeMillis();
        // 1. 获取所有用户行为
        List<UserBehavior> behaviors = userBehaviorRepository.findAll();
        if (behaviors.isEmpty()) {
            return;
        }

        // 2. 基于权重的用户-物品集合与计数
        //    - 点击权重固定 0.5
        //    - 阅读时长映射到 0-5（<30s 为 0，≥40min 为 5，其余线性）
        //    - 收藏/书架/评论/评分沿用原值
        Map<Long, Map<Long, Double>> userItemWeights = new HashMap<>();
        Map<Long, Integer> itemInteractionCount = new HashMap<>();
        for (UserBehavior b : behaviors) {
            Long u = b.getUserId();
            Long item = b.getBookId();
            double w = mapBehaviorToWeight(b);
            if (w <= 0) continue; // 无效权重不计入
            Map<Long, Double> wm = userItemWeights.computeIfAbsent(u, k -> new HashMap<>());
            // 同一本书多行为，保留更大权重
            wm.put(item, Math.max(wm.getOrDefault(item, 0.0), w));
        }
        // 物品被多少用户有效交互（用于相似度归一化）
        for (Map<Long, Double> wm : userItemWeights.values()) {
            for (Map.Entry<Long, Double> e : wm.entrySet()) {
                if (e.getValue() > 0) {
                    itemInteractionCount.put(e.getKey(), itemInteractionCount.getOrDefault(e.getKey(), 0) + 1);
                }
            }
        }

        // 3. 计算共现矩阵 C[i][j] (使用 IUF 优化)
        // 使用 ConcurrentHashMap 以支持并行计算
        Map<Long, Map<Long, Double>> cooccurrenceMatrix = new ConcurrentHashMap<>();
        
        userItemWeights.entrySet().parallelStream().forEach(entry -> {
            Map<Long, Double> items = entry.getValue();
            if (items.size() <= 1) return;
            // IUF：惩罚活跃用户（按有效交互数）
            double iuf = 1.0 / Math.log(1.0 + items.size());
            List<Map.Entry<Long, Double>> list = new ArrayList<>(items.entrySet());
            for (int i = 0; i < list.size(); i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    Long item1 = list.get(i).getKey();
                    Long item2 = list.get(j).getKey();
                    double w1 = list.get(i).getValue();
                    double w2 = list.get(j).getValue();
                    double perUser = Math.min(w1, w2) * iuf;
                    if (perUser <= 0) continue;
                    updateCooccurrence(cooccurrenceMatrix, item1, item2, perUser);
                    updateCooccurrence(cooccurrenceMatrix, item2, item1, perUser);
                }
            }
        });

        // 4. 计算相似度并只保留 Top K（配置：recommend.topK）
        Map<Long, Map<String, String>> finalSimilarities = new HashMap<>();
        
        cooccurrenceMatrix.forEach((item1, neighbors) -> {
            // 使用优先队列实现 Top K 过滤 (小顶堆)
            PriorityQueue<Map.Entry<Long, Double>> pq = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue)
            );
            
            double n1 = itemInteractionCount.get(item1);
            
            for (Map.Entry<Long, Double> entry : neighbors.entrySet()) {
                Long item2 = entry.getKey();
                double coCount = entry.getValue();
                
                // 过滤掉共现权重太小的 (最小阈值)
                if (coCount < 0.1) continue; 
                
                double n2 = itemInteractionCount.get(item2);
                double sim = coCount / Math.sqrt(n1 * n2);
                
                pq.offer(new AbstractMap.SimpleEntry<>(item2, sim));
                if (pq.size() > topK) {
                    pq.poll();
                }
            }
            
            // 转换为 Redis 存储格式
            Map<String, String> simMap = new HashMap<>();
            while (!pq.isEmpty()) {
                Map.Entry<Long, Double> e = pq.poll();
                simMap.put(e.getKey().toString(), String.format("%.6f", e.getValue()));
            }
            if (!simMap.isEmpty()) {
                finalSimilarities.put(item1, simMap);
            }
        });

        // 5. 批量写入 Redis 哈希：item_sim:{itemId} -> { neighborId: score }
        finalSimilarities.forEach((itemId, simMap) -> {
            String key = "item_sim:" + itemId;
            try {
                if (redisHasKey(key)) {
                    redisDelete(key);
                }
                redisHashPutAll(key, simMap);
            } catch (Exception ignored) {}
        });

        long endTime = System.currentTimeMillis();
        System.out.println("物品相似度计算完成 (IUF-ItemCF), 耗时: " + (endTime - startTime) / 1000.0 + " 秒");
    }
    
    private boolean redisHasKey(String key) {
        if (stringRedisTemplate == null) return false;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
    
    private void redisDelete(String key) {
        if (stringRedisTemplate == null) return;
        stringRedisTemplate.delete(key);
    }
    
    @SuppressWarnings("unchecked")
    private void redisHashPutAll(String key, Map<String, String> map) {
        if (stringRedisTemplate == null) return;
        stringRedisTemplate.opsForHash().putAll(key, map);
    }

    // 将行为映射到权重（0~5）
    private double mapBehaviorToWeight(UserBehavior b) {
        String t = b.getBehaviorType();
        if (UserBehavior.CLICK.equals(t)) {
            return 0.5;
        }
        if (UserBehavior.VIEW.equals(t)) {
            // score 存的是累计阅读秒数
            double seconds = b.getScore() != null ? b.getScore() : 0.0;
            if (seconds < 30) return 0.0;
            double maxSec = 2400.0; // 40 分钟
            if (seconds >= maxSec) return 5.0;
            return 5.0 * (seconds - 30.0) / (maxSec - 30.0);
        }
        if (UserBehavior.COLLECT.equals(t)) return 3.0;
        if (UserBehavior.ADD_SHELF.equals(t)) return 4.0;
        if (UserBehavior.REVIEW.equals(t)) return 4.0;
        if (UserBehavior.RATE.equals(t)) {
            double v = b.getScore() != null ? b.getScore() : 0.0;
            if (v < 0) v = 0;
            if (v > 5) v = 5;
            return v;
        }
        return 0.0;
    }
    private void updateCooccurrence(Map<Long, Map<Long, Double>> matrix, Long i, Long j, double weight) {
        matrix.computeIfAbsent(i, k -> new ConcurrentHashMap<>())
              .merge(j, weight, Double::sum);
    }

    @Override
    public long countAllBehaviors() {
        return userBehaviorRepository.count();
    }


    @Override
    public Map<Long, Double> getUserCategoryPreferences(Long userId) {
        List<Object[]> categoryScores = userBehaviorRepository.findCategoryScoresByUserId(userId);
        
        if (categoryScores == null || categoryScores.isEmpty()) {
            return new HashMap<>();
        }

        // 计算总分数用于归一化
        double totalScore = categoryScores.stream()
                .mapToDouble(score -> ((Number) score[1]).doubleValue())
                .sum();

        if (totalScore == 0) {
            return new HashMap<>();
        }

        // 转换为分类ID到归一化权重的映射
        Map<Long, Double> preferences = new HashMap<>();
        for (Object[] score : categoryScores) {
            if (score[0] != null) {
                preferences.put((Long) score[0], ((Number) score[1]).doubleValue() / totalScore);
            }
        }
        return preferences;
    }

    @Override
    public List<Long> getRecommendedBooks(Long userId, int limit) {
        // 已交互图书集合（用于去重）
        List<UserBehavior> behaviors = userBehaviorRepository.findByUserId(userId);
        Set<Long> interactedBookIds = behaviors.stream()
                .map(UserBehavior::getBookId)
                .collect(Collectors.toSet());

        // 种子选取：按行为权重降序，合并同一本书取更大权重，选 Top-N（配置：recommend.seedLimit）
        Map<Long, Double> seedWeights = new HashMap<>();
        for (UserBehavior b : behaviors) {
            double w = mapBehaviorToWeight(b);
            if (w <= 0) continue;
            seedWeights.merge(b.getBookId(), w, Math::max);
        }
        List<Map.Entry<Long, Double>> seeds = new ArrayList<>(seedWeights.entrySet());
        seeds.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        if (seeds.size() > this.seedLimit) {
            seeds = seeds.subList(0, this.seedLimit);
        }

        // 邻域召回：对每个种子从 Redis 的 item_sim 读取邻居，累加 sim×weight(seed)
        Map<Long, Double> candidateScores = new HashMap<>();
        for (Map.Entry<Long, Double> seed : seeds) {
            Long seedId = seed.getKey();
            double sw = seed.getValue();
            Map<Object, Object> simMap = null;
            if (stringRedisTemplate != null) {
                String key = "item_sim:" + seedId;
                try {
                    simMap = stringRedisTemplate.opsForHash().entries(key);
                } catch (Exception ignored) {}
            }
            if (simMap == null || simMap.isEmpty()) continue;

            // 限制邻域 TopK（配置：recommend.topK）
            List<Map.Entry<Object, Object>> entries = new ArrayList<>(simMap.entrySet());
            entries.sort((a, b) -> {
                double sv = 0, tv = 0;
                try { sv = Double.parseDouble(a.getValue().toString()); } catch (Exception ignored) {}
                try { tv = Double.parseDouble(b.getValue().toString()); } catch (Exception ignored) {}
                return Double.compare(tv, sv);
            });
            int maxN = Math.min(topK, entries.size());
            for (int i = 0; i < maxN; i++) {
                Map.Entry<Object, Object> e = entries.get(i);
                Long neighborId;
                try {
                    neighborId = Long.valueOf(e.getKey().toString());
                } catch (Exception ex) {
                    continue;
                }
                if (neighborId.equals(seedId)) continue;
                if (interactedBookIds.contains(neighborId)) continue;
                double sim;
                try {
                    sim = Double.parseDouble(e.getValue().toString());
                } catch (Exception ex) {
                    continue;
                }
                if (sim <= 0) continue;
                candidateScores.merge(neighborId, sim * sw, Double::sum);
            }
        }

        List<Long> recommendedIds = new ArrayList<>();
        // 若存在邻域召回候选，融合分类偏好进行再排序与多样化
        if (!candidateScores.isEmpty()) {
            Map<Long, Double> categoryPreferences = getUserCategoryPreferences(userId);
            double alphaLocal = alpha; // 配置：recommend.alpha

            Map<Long, Double> fusedScores = new HashMap<>();
            Map<Long, Integer> categoryCounts = new HashMap<>();
            int perCategoryCap = Math.max(1, (int) Math.ceil(limit * categoryCapRatio));

            for (Map.Entry<Long, Double> entry : candidateScores.entrySet()) {
                Long cid = null;
                try {
                    Book b = bookRepository.findById(entry.getKey()).orElse(null);
                    if (b != null) cid = b.getCategoryId();
                } catch (Exception ignored) {}
                double pref = (cid != null) ? categoryPreferences.getOrDefault(cid, 0.0) : 0.0;
                double fused = entry.getValue() * (1 + alphaLocal * pref);
                fusedScores.put(entry.getKey(), fused);
            }

            List<Map.Entry<Long, Double>> sorted = new ArrayList<>(fusedScores.entrySet());
            sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            for (Map.Entry<Long, Double> e : sorted) {
                if (recommendedIds.size() >= limit) break;
                Long bid = e.getKey();
                Book b = null;
                try { b = bookRepository.findById(bid).orElse(null); } catch (Exception ignored) {}
                if (b == null) continue;
                Long catId = b.getCategoryId();
                int used = categoryCounts.getOrDefault(catId, 0);
                if (used >= perCategoryCap) continue;
                recommendedIds.add(bid);
                categoryCounts.put(catId, used + 1);
            }
        }

        // 若相似召回不足，回退到“分类偏好 + 高分书”方案补齐
        if (recommendedIds.size() < limit) {
            int remaining = limit - recommendedIds.size();

            Map<Long, Double> categoryPreferences = getUserCategoryPreferences(userId);
            if (!categoryPreferences.isEmpty()) {
                List<Long> topCategories = categoryPreferences.entrySet().stream()
                        .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                        .limit(3)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                int booksPerCategory = (int) Math.ceil((double) remaining / Math.max(1, topCategories.size()));
                for (Long categoryId : topCategories) {
                    List<Book> booksInCatalog = bookRepository
                            .findByCategoryId(categoryId, PageRequest.of(0, booksPerCategory + interactedBookIds.size()))
                            .getContent();
                    for (Book book : booksInCatalog) {
                        Long bid = book.getBookId();
                        if (interactedBookIds.contains(bid)) continue;
                        if (recommendedIds.contains(bid)) continue;
                        recommendedIds.add(bid);
                        if (recommendedIds.size() >= limit) break;
                    }
                    if (recommendedIds.size() >= limit) break;
                }
            }

            // 仍不足则用全站高分图书补齐
            if (recommendedIds.size() < limit) {
                List<Book> popularBooks = bookRepository
                        .findByStatusOrderByRatingDesc(1, PageRequest.of(0, limit * 2))
                        .getContent();
                for (Book book : popularBooks) {
                    Long bid = book.getBookId();
                    if (interactedBookIds.contains(bid)) continue;
                    if (recommendedIds.contains(bid)) continue;
                    recommendedIds.add(bid);
                    if (recommendedIds.size() >= limit) break;
                }
            }
        }

        return recommendedIds.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int syncAllBehaviors() {
        int count = 0;

        // 1. 同步浏览历史 (VIEW, score=1.0)
        List<BrowsingHistory> histories = browsingHistoryRepository.findAll();
        for (BrowsingHistory history : histories) {
            Book book = bookRepository.findById(history.getBookId()).orElse(null);
            if (book != null) {
                recordUserBehavior(history.getUserId(), history.getBookId(), book.getCategoryId(), 
                                 UserBehavior.VIEW, UserBehavior.SCORE_VIEW);
                count++;
            }
        }

        // 2. 同步收藏 (COLLECT, score=3.0)
        List<UserCollection> collections = userCollectionRepository.findAll();
        for (UserCollection collection : collections) {
            Book book = bookRepository.findById(collection.getBookId()).orElse(null);
            if (book != null) {
                String type = (collection.getCollectionType() == 2) ? UserBehavior.ADD_SHELF : UserBehavior.COLLECT;
                Double score = (collection.getCollectionType() == 2) ? UserBehavior.SCORE_ADD_SHELF : UserBehavior.SCORE_COLLECT;
                recordUserBehavior(collection.getUserId(), collection.getBookId(), book.getCategoryId(), type, score);
                count++;
            }
        }

        // 3. 同步评分 (RATE, score=用户评分)
        List<Rating> ratings = ratingRepository.findAll();
        for (Rating rating : ratings) {
            Book book = bookRepository.findById(rating.getBookId()).orElse(null);
            if (book != null) {
                recordUserBehavior(rating.getUserId(), rating.getBookId(), book.getCategoryId(), 
                                 UserBehavior.RATE, rating.getScore().doubleValue());
                count++;
            }
        }

        // 4. 同步评论 (REVIEW, score=4.0)
        List<Review> reviews = reviewRepository.findAll();
        for (Review review : reviews) {
            Book book = bookRepository.findById(review.getBookId()).orElse(null);
            if (book != null) {
                recordUserBehavior(review.getUserId(), review.getBookId(), book.getCategoryId(), 
                                 UserBehavior.REVIEW, UserBehavior.SCORE_REVIEW);
                count++;
            }
        }

        return count;
    }

    @Override
    @Transactional
    public void clearAllBehaviors() {
        userBehaviorRepository.deleteAll();
    }
}
