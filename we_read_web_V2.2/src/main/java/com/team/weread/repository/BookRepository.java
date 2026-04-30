package com.team.weread.repository;

import com.team.weread.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 图书数据访问接口
 * <p>
 * 提供图书相关的数据库操作方法
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * 根据标题模糊查询
     * @param title 标题关键词
     * @param pageable 分页参数
     * @return 符合条件的图书分页结果
     */
    Page<Book> findByTitleContaining(String title, Pageable pageable);

    /**
     * 根据作者模糊查询
     * @param authorName 作者关键词
     * @param pageable 分页参数
     * @return 符合条件的图书分页结果
     */
    @Query("SELECT b FROM Book b WHERE b.author LIKE %:authorName%")
    Page<Book> findByAuthorContaining(@Param("authorName") String authorName, Pageable pageable);

    /**
     * 根据分类ID查询
     * @param categoryId 分类ID
     * @param pageable 分页参数
     * @return 符合条件的图书分页结果
     */
    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * 根据分类名称查询
     * @param name 分类名称
     * @param pageable 分页参数
     * @return 符合条件的图书分页结果
     */
    Page<Book> findByCategoryName(String name, Pageable pageable);

    /**
     * 根据ISBN查询
     * @param isbn ISBN编号
     * @return 符合条件的图书，如果不存在则返回null
     */
    Book findByIsbn(String isbn);

   
    /**
     * 根据评分范围查询
     * @param minRating 最低评分
     * @param maxRating 最高评分
     * @param pageable 分页参数
     * @return 符合条件的图书分页结果
     */
    Page<Book> findByRatingBetween(Double minRating, Double maxRating, Pageable pageable);

    /**
     * 复合查询：标题或作者或描述包含关键字
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 符合条件的图书分页结果
     */
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.description LIKE %:keyword% OR b.author LIKE %:keyword%")
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 扩展复合查询：书名（模糊）、作者（模糊）、描述（模糊）、ISBN（模糊）、分类名（模糊）
     */
    @Query("SELECT b FROM Book b LEFT JOIN b.category c " +
           "WHERE (b.title LIKE %:keyword% " +
           "OR b.author LIKE %:keyword% " +
           "OR b.description LIKE %:keyword% " +
           "OR b.isbn LIKE %:keyword% " +
           "OR (c IS NOT NULL AND c.name LIKE %:keyword%))")
    Page<Book> searchByKeywordExtended(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 获取推荐书籍（根据评分排序）
     * @param status 图书状态，通常为1（正常）
     * @param pageable 分页参数
     * @return 符合条件的图书分页结果，按评分降序排序
     */
    Page<Book> findByStatusOrderByRatingDesc(Integer status, Pageable pageable);

    /**
     * 获取最新上架的书籍
     * @param status 图书状态，通常为1（正常）
     * @param pageable 分页参数
     * @return 符合条件的图书分页结果，按创建时间降序排序
     */
    Page<Book> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    /**
     * 获取最受欢迎的书籍（根据浏览量排序）
     * @param status 图书状态
     * @param pageable 分页参数
     * @return 符合条件的图书分页结果
     */
    Page<Book> findByStatusOrderByViewCountDesc(Integer status, Pageable pageable);

    /**
     * 获取收藏最多的书籍（根据收藏量排序）
     * @param status 图书状态
     * @param pageable 分页参数
     * @return 符合条件的图书分页结果
     */
    Page<Book> findByStatusOrderByCollectionCountDesc(Integer status, Pageable pageable);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.category WHERE b.bookId = :id")
    Book findByIdWithCategory(@Param("id") Long id);

    /**
     * 管理员图书列表高级查询
     * @param keyword 搜索关键词（匹配书名、作者、ISBN）
     * @param categoryId 分类ID
     * @param status 图书状态
     * @param hasEbook 是否有电子书
     * @param pageable 分页参数
     * @return 符合条件的图书分页结果
     */
    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.category c WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.isbn LIKE %:keyword%) AND " +
           "(:categoryId IS NULL OR b.categoryId = :categoryId) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:hasEbook IS NULL OR b.hasEbook = :hasEbook)")
    Page<Book> findBooksByAdminCriteria(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") Byte status,
            @Param("hasEbook") Boolean hasEbook,
            Pageable pageable);
}
