package com.team.weread.security;

import com.team.weread.model.User;
import com.team.weread.service.JwtBlacklistService;
import com.team.weread.service.UserService;
import com.team.weread.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT认证过滤器
 * <p>
 * 用于拦截请求，验证JWT令牌并设置认证信息
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * JWT黑名单服务
     */
    @Autowired
    private JwtBlacklistService jwtBlacklistService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // 如果是管理员接口，不在此过滤器处理
        return path.startsWith("/api/admin/");
    }

    /**
     * 过滤器内部处理方法
     * <p>
     * 从请求头中提取JWT令牌，验证并设置认证信息
     * </p>
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 从请求头中获取Authorization字段
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 检查Authorization头是否存在且格式正确
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // 提取JWT令牌（去掉"Bearer "前缀）
            jwt = authorizationHeader.substring(7);

            // 简单校验JWT格式（必须包含两个点号），避免无效格式触发解析异常
            if (jwt.split("\\.").length != 3) {
                logger.warn("接收到格式错误的JWT令牌: " + jwt);
                filterChain.doFilter(request, response);
                return;
            }

            try {
                // 检查令牌是否在黑名单中
                if (jwtBlacklistService.isBlacklisted(jwt)) {
                    logger.warn("尝试使用已加入黑名单的JWT令牌");
                    filterChain.doFilter(request, response);
                    return;
                }

                // 从JWT中提取用户名
                username = JwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // 如果是刷新 Token 接口，即使令牌过期也尝试提取用户名
                if (request.getServletPath().equals("/api/user/auth/refresh-token")) {
                    try {
                        username = JwtUtil.extractUsername(jwt);
                    } catch (Exception ex) {
                        logger.error("刷新Token时提取用户名失败: " + ex.getMessage());
                    }
                }

                if (username == null) {
                    logger.error("JWT令牌无效: " + e.getMessage());
                    // 如果令牌无效且不是刷新接口，直接让请求继续
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }

        // 如果成功提取到用户名且当前SecurityContext中没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 根据用户名查找用户
            User user = userService.findByUsername(username);

            // 验证用户存在且JWT有效
            if (user != null) {
                // 如果是刷新 Token 接口，不需要验证 Token 是否过期（因为我们就是要刷新过期的）
                boolean isRefreshPath = request.getServletPath().equals("/api/user/auth/refresh-token");
                if (isRefreshPath || JwtUtil.validateToken(jwt, username)) {
                    // 创建认证令牌，使用用户名字符串作为principal
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, new ArrayList<>());
                    // 设置认证详情
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // 将认证信息设置到SecurityContext中
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }


        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}
