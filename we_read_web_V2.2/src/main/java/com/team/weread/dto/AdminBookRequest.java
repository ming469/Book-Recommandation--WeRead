package com.team.weread.dto;

import com.team.weread.model.Book;
import com.team.weread.model.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员图书操作请求类
 * <p>
 * 用于管理员添加、修改图书时的参数封装
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookRequest {
    private Book book;
    private Category category;
}
