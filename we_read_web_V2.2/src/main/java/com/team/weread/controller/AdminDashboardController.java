package com.team.weread.controller;

import com.team.weread.model.AuditLog;
import com.team.weread.model.TodoTask;
import com.team.weread.repository.AuditLogRepository;
import com.team.weread.repository.TodoTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private TodoTaskRepository todoTaskRepository;

    private Map<String, Object> buildMeta(int code, String message) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("code", code);
        meta.put("message", message);
        return meta;
    }

    /**
     * 获取最近活动日志
     */
    @GetMapping("/activities")
    public ResponseEntity<Map<String, Object>> getActivities() {
        List<AuditLog> list = auditLogRepository.findTop20ByOrderByTimeDesc();
        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "获取活动日志成功"));
        response.put("data", list);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取待办事项列表
     */
    @GetMapping("/todos")
    public ResponseEntity<Map<String, Object>> getTodos() {
        List<TodoTask> list = todoTaskRepository.findAllByOrderByCreatedAtDesc();
        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "获取待办事项成功"));
        response.put("data", list);
        return ResponseEntity.ok(response);
    }

    /**
     * 添加待办事项
     */
    @PostMapping("/todos")
    public ResponseEntity<Map<String, Object>> addTodo(@RequestBody TodoTask task) {
        TodoTask saved = todoTaskRepository.save(task);
        Map<String, Object> response = new HashMap<>();
        response.put("meta", buildMeta(200, "添加待办成功"));
        response.put("data", saved);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新待办状态
     */
    @PutMapping("/todos/{id}")
    public ResponseEntity<Map<String, Object>> updateTodo(@PathVariable Long id, @RequestBody TodoTask task) {
        TodoTask existing = todoTaskRepository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.status(404).body(Map.of("meta", buildMeta(404, "待办不存在")));
        }
        existing.setCompleted(task.isCompleted());
        if (task.getContent() != null) {
            existing.setContent(task.getContent());
        }
        todoTaskRepository.save(existing);
        return ResponseEntity.ok(Map.of("meta", buildMeta(200, "更新成功"), "data", existing));
    }

    /**
     * 删除待办事项
     */
    @DeleteMapping("/todos/{id}")
    public ResponseEntity<Map<String, Object>> deleteTodo(@PathVariable Long id) {
        if (!todoTaskRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("meta", buildMeta(404, "待办不存在")));
        }
        todoTaskRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("meta", buildMeta(200, "删除成功")));
    }
}
