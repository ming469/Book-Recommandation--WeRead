package com.team.weread.service.impl;

import com.team.weread.model.Book;
import com.team.weread.model.BookStats;
import com.team.weread.model.Category;
import com.team.weread.repository.BookRepository;
import com.team.weread.repository.BookStatsRepository;
import com.team.weread.repository.CategoryRepository;
import com.team.weread.service.BookService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 图书服务实现类
 */
@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookStatsRepository bookStatsRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Book updateBook(Book book, Category category) {
        Book existingBook = bookRepository.findById(book.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("图书不存在：" + book.getBookId()));

        Book bookWithSameIsbn = bookRepository.findByIsbn(book.getIsbn());
        if (bookWithSameIsbn != null && !bookWithSameIsbn.getBookId().equals(book.getBookId())) {
            throw new IllegalArgumentException("ISBN已被其他图书使用：" + book.getIsbn());
        }

        Category existingCategory = categoryRepository.findByName(category.getName());
        if (existingCategory == null) {
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());
            existingCategory = categoryRepository.save(category);
        }
        category = existingCategory;

        existingBook.setTitle(book.getTitle());
        existingBook.setIsbn(book.getIsbn());
        existingBook.setDescription(book.getDescription());
        existingBook.setCategory(category);
        existingBook.setPublisher(book.getPublisher());
        existingBook.setPublicationDate(book.getPublicationDate());
        existingBook.setFilePath(book.getFilePath());
        existingBook.setStatus(book.getStatus());
        existingBook.setUpdatedAt(LocalDateTime.now());
        existingBook.setAuthor(book.getAuthor());

        return bookRepository.save(existingBook);
    }

    @Override
    public Page<Book> getBooksByAdminCriteria(String keyword, Long categoryId, Byte status, Boolean hasEbook, Pageable pageable) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        return bookRepository.findBooksByAdminCriteria(keyword, categoryId, status, hasEbook, pageable);
    }

    @Override
    @Transactional
    public Book addBook(Book book, Category category) {
        Book existingBook = bookRepository.findByIsbn(book.getIsbn());
        if (existingBook != null) {
            throw new IllegalArgumentException("ISBN已存在：" + book.getIsbn());
        }

        Category existingCategory = categoryRepository.findByName(category.getName());
        if (existingCategory == null) {
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());
            existingCategory = categoryRepository.save(category);
        }
        category = existingCategory;

        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        book.setCategory(category);
        
        Book savedBook = bookRepository.save(book);

        BookStats bookStats = new BookStats(savedBook.getBookId());
        bookStats.setCollectionCount(0);
        bookStats.setDownloadCount(0);
        bookStats.setRatingCount(0);
        bookStats.setRatingSum(0);
        bookStats.setViewCount(0);
        bookStats.setUpdatedAt(LocalDateTime.now());
        bookStatsRepository.save(bookStats);

        return savedBook;
    }

    @Override
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Override
    public Book getBookById(Long id) {
        return bookRepository.findByIdWithCategory(id);
    }

    @Override
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    @Override
    public Page<Book> searchBooks(String keyword, Pageable pageable) {
        if (keyword == null) {
            return Page.empty(pageable);
        }
        String k = keyword.trim();
        return bookRepository.searchByKeywordExtended(k, pageable);
    }

    @Override
    public Page<Book> getBooksByCategory(String category, Pageable pageable) {
        try {
            Long id = Long.parseLong(category);
            return bookRepository.findByCategoryId(id, pageable);
        } catch (NumberFormatException e) {
            return bookRepository.findByCategoryName(category, pageable);
        }
    }

    @Override
    public Page<Book> getRecommendedBooks(Pageable pageable) {
        return bookRepository.findByStatusOrderByRatingDesc(1, pageable);
    }

    @Override
    public Page<Book> getLatestBooks(Pageable pageable) {
        return bookRepository.findByStatusOrderByCreatedAtDesc(1, pageable);
    }

    @Override
    public Page<Book> getPopularBooks(Pageable pageable) {
        return bookRepository.findByStatusOrderByViewCountDesc(1, pageable);
    }

    @Override
    public Page<Book> getMostCollectedBooks(Pageable pageable) {
        return bookRepository.findByStatusOrderByCollectionCountDesc(1, pageable);
    }

    @Override
    public Page<Book> searchBooksByAuthor(String authorName, Pageable pageable) {
        return bookRepository.findByAuthorContaining(authorName, pageable);
    }

    @Override
    public Page<Book> getBooksByRating(Double minRating, Double maxRating, Pageable pageable) {
        return bookRepository.findByRatingBetween(minRating, maxRating, pageable);
    }

    @Override
    public long countTotalBooks() {
        return bookRepository.count();
    }

    @Override
    @Transactional
    public boolean deleteBook(Long id) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            return false;
        }
        bookStatsRepository.deleteByBookId(id);
        bookRepository.deleteById(id);
        return true;
    }
}
