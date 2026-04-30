package com.team.weread.service;

import java.util.Date;

/**
 * JWT黑名单服务接口
 */
public interface JwtBlacklistService {

    /**
     * 将令牌加入黑名单
     */
    void addToBlacklist(String token, Date expiryDate);

    /**
     * 检查令牌是否在黑名单中
     */
    boolean isBlacklisted(String token);

    /**
     * 清理已过期的黑名单条目
     */
    void cleanupExpiredTokens();
}
