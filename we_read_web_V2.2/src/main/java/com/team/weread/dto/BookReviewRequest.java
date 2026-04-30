package com.team.weread.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 图书评论请求类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BookReviewRequest extends UserBehaviorRequest {
    private String title;
    private String content;

    public BookReviewRequest() {}
}
