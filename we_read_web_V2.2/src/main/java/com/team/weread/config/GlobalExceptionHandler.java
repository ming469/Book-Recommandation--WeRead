package com.team.weread.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch() {
        Map<String, Object> response = new HashMap<>();
        response.put("meta", Map.of(
            "code", 400,
            "message", "参数类型错误：图书ID必须为数字"
        ));
        response.put("data", null);
        return ResponseEntity.badRequest().body(response);
    }
}