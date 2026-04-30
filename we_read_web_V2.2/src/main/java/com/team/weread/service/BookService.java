package com.team.weread.service;

import com.team.weread.model.Book;
import com.team.weread.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 图书服务接口
 * <p>
 * 定义图书相关的业务逻辑操作
 * </p>
 */
public interface BookService {

    /**
     * 更新图书信息
     */
    Book updateBook(Book book, Category category);

    /**
     * 管理员图书列表高级查询
     */
    Page<Book> getBooksByAdminCriteria(String keyword, Long categoryId, Byte status, Boolean hasEbook, Pageable pageable);

    /**
     * 添加新图书
     */
    Book addBook(Book book, Category category);

    /**
     * 获取所有图书（分页）
     */
    Page<Book> getAllBooks(Pageable pageable);

    /**
     * 通过ID获取图书
     */
    Book getBookById(Long id);

    /**
     * 根据ISBN获取图书
     */
    Book getBookByIsbn(String isbn);

    /**
     * 搜索图书
     */
    Page<Book> searchBooks(String keyword, Pageable pageable);

    /**
     * 根据分类获取图书
     */
    Page<Book> getBooksByCategory(String category, Pageable pageable);

    /**
     * 获取推荐图书
     */
    Page<Book> getRecommendedBooks(Pageable pageable);

    /**
     * 获取最新图书
     */
    Page<Book> getLatestBooks(Pageable pageable);

    /**
     * 获取最受欢迎图书
     */
    Page<Book> getPopularBooks(Pageable pageable);

    /**
     * 获取收藏最多图书
     */
    Page<Book> getMostCollectedBooks(Pageable pageable);

    /**
     * 根据作者获取图书
     */
    Page<Book> searchBooksByAuthor(String authorName, Pageable pageable);

    /**
     * 根据评分范围获取图书
     */
    Page<Book> getBooksByRating(Double minRating, Double maxRating, Pageable pageable);

    /**
     * 获取图书总数
     */
    long countTotalBooks();

    /**
     * 删除图书
     */
    boolean deleteBook(Long id);
}
