package com.team.weread.service;

/**
 * 认证事件服务接口
 */
public interface AuthEventService {

    /**
     * 记录用户登出事件
     */
    void logLogoutEvent(String username, String ipAddress, boolean success, String message);
}
