package com.team.weread.service.impl;

import com.team.weread.service.AuthEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 认证事件服务实现类
 */
@Service
public class AuthEventServiceImpl implements AuthEventService {

    private static final Logger logger = LoggerFactory.getLogger(AuthEventServiceImpl.class);

    @Override
    public void logLogoutEvent(String username, String ipAddress, boolean success, String message) {
        String logMessage = String.format(
                "用户登出 - 用户名: %s, IP地址: %s, 时间: %s, 状态: %s, 消息: %s",
                username,
                ipAddress,
                new Date(),
                success ? "成功" : "失败",
                message
        );

        if (success) {
            logger.info(logMessage);
        } else {
            logger.warn(logMessage);
        }
    }
}
