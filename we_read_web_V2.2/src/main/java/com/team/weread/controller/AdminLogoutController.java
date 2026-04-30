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
@RequestMapping("/api/admin/auth")
public class AdminLogoutController {

    @Autowired
    private JwtBlacklistService jwtBlacklistService;

    @Autowired
    private AuthEventService authEventService;


    /**
     * 管理员登出接口
     * <p>
     * 清除当前管理员的认证信息，并将JWT令牌加入黑名单
     * </p>
     *
     * @param request HTTP请求对象
     * @return 登出结果响应
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        // 创建响应Map
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> meta = new HashMap<>();

        // 获取JWT令牌并验证
        String token = extractTokenFromRequest(request);
        if (token == null || token.isEmpty()) {
            meta.put("code", 401);
            meta.put("message", "未检测到有效的认证令牌");
            response.put("meta", meta);
            response.put("data", null);
            return ResponseEntity.status(401).body(response);
        }

        // 获取客户端IP地址
        String ipAddress = getClientIpAddress(request);

        // 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

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

        // 将令牌加入黑名单
        try {
            jwtBlacklistService.addToBlacklist(token, JwtUtil.extractExpiration(token));
        } catch (Exception e) {
            meta.put("code", 401);
            meta.put("message", "令牌验证失败");
            response.put("meta", meta);
            response.put("data", null);
            // 记录错误事件
            authEventService.logLogoutEvent(username, ipAddress, false, "令牌解析失败: " + e.getMessage());
            return ResponseEntity.status(401).body(response);
        }

        // 清除安全上下文
        SecurityContextHolder.clearContext();

        // 记录成功的登出事件
        authEventService.logLogoutEvent(username, ipAddress, true, "管理员成功登出");

        // 返回成功响应
        meta.put("code", 200);
        meta.put("message", "管理员登出成功");
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
