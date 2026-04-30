package com.team.weread.controller;

import com.team.weread.service.AuthEventService;
import com.team.weread.service.JwtBlacklistService;
import com.team.weread.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户登出控制器
 * <p>
 * 处理用户退出登录相关的请求
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/user/auth")
public class LogoutController {

    @Autowired
    private JwtBlacklistService jwtBlacklistService;

    @Autowired
    private AuthEventService authEventService;


    /**
     * 用户登出接口
     * <p>
     * 清除当前用户的认证信息，并将JWT令牌加入黑名单
     * </p>
     *
     * @param request HTTP请求对象
     * @return 登出结果响应
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        // 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 获取客户端IP地址
        String ipAddress = getClientIpAddress(request);

        // 创建响应Map
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> meta = new HashMap<>();

        // 检查认证信息是否为空
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            meta.put("code", 401); // 未授权
            meta.put("message", "未检测到登录信息或登录已失效");
            response.put("meta", meta);
            response.put("data", null);

            return ResponseEntity.status(401).body(response);
        }

        // 获取用户名
        String username = authentication.getName();

        // 获取JWT令牌
        String token = extractTokenFromRequest(request);

        // 如果找到令牌，将其加入黑名单
        if (token != null && !token.isEmpty()) {
            try {
                jwtBlacklistService.addToBlacklist(token, JwtUtil.extractExpiration(token));
            } catch (Exception e) {
                // 令牌解析失败，记录错误但继续登出流程
                authEventService.logLogoutEvent(username, ipAddress, false, "令牌解析失败: " + e.getMessage());
            }
        }

        // 清除安全上下文
        SecurityContextHolder.clearContext();

        // 记录成功的登出事件
        authEventService.logLogoutEvent(username, ipAddress, true, "用户成功登出");

        // 返回成功响应
        meta.put("code", 200);
        meta.put("message", "用户登出成功");
        response.put("meta", meta);
        response.put("data", null);

        return ResponseEntity.ok(response);
    }

    /**
     * 从请求中提取JWT令牌
     *
     * @param request HTTP请求
     * @return JWT令牌，如果未找到则返回null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // 可能包含多个IP，第一个是客户端真实IP
            return xForwardedFor.split(",")[0].trim();
        }

        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            return remoteAddr;
        }

        return "unknown";
    }
}
