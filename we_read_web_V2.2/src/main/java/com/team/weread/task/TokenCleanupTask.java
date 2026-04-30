package com.team.weread.task;

import com.team.weread.service.JwtBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 令牌清理定时任务
 * <p>
 * 定期清理已过期的黑名单令牌，释放内存
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@Component
public class TokenCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupTask.class);

    @Autowired
    private JwtBlacklistService jwtBlacklistService;

    /**
     * 定期清理过期的黑名单令牌
     * 默认每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanupExpiredTokens() {
        logger.info("开始清理过期的黑名单令牌");
        jwtBlacklistService.cleanupExpiredTokens();
        logger.info("过期黑名单令牌清理完成");
    }
}