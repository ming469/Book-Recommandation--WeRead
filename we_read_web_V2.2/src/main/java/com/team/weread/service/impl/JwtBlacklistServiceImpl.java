package com.team.weread.service.impl;

import com.team.weread.service.JwtBlacklistService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT黑名单服务实现类
 */
@Service
public class JwtBlacklistServiceImpl implements JwtBlacklistService {

    private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    @Override
    public void addToBlacklist(String token, Date expiryDate) {
        blacklistedTokens.put(token, expiryDate);
        cleanupExpiredTokens();
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    @Override
    public void cleanupExpiredTokens() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }
}
