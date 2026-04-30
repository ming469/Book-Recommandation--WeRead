package com.team.weread.dto;

import lombok.Data;

/**
 * 用户图书行为请求类
 * <p>
 * 用于接收用户图书行为记录（评分、分类等）的请求参数
 * </p>
 */
@Data
public class UserBehaviorRequest {
    /**
     * 图书ID（必填）
     */
    private Long bookId;
    
    /**
     * 分类ID（必填）
     */
    private Long categoryId;
    
    /**
     * 评分值（评分行为时必填）
     * 范围：1-5
     */
    private Integer rating;
    
    /**
     * 评分值（评分行为时必填）- 兼容前端rate字段
     * 范围：0.5-5.0，步长0.5
     */
    private Double rate;
}
