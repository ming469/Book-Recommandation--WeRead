package com.team.weread.dto;

import lombok.Data;

/**
 * 修改密码请求DTO
 */
@Data
public class ChangePasswordRequest {
    private String account;
    private String oldPassword;
    private String newPassword;
}
