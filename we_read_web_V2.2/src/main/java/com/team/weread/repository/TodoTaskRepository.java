package com.team.weread.repository;

import com.team.weread.model.TodoTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TodoTaskRepository extends JpaRepository<TodoTask, Long> {
    List<TodoTask> findAllByOrderByCreatedAtDesc();
}
