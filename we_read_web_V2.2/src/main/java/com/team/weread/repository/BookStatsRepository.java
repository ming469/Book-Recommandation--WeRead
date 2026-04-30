package com.team.weread.repository;
import com.team.weread.model.BookStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookStatsRepository extends JpaRepository<BookStats, Long> {
    BookStats findByBookId(Long bookId);
    void deleteByBookId(Long bookId);
}