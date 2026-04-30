package com.team.weread.config;

import com.team.weread.security.JwtAuthenticationFilter;
import com.team.weread.security.AdminJwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 系统安全配置类
 * <p>
 * 该类负责应用的安全相关配置，包括：
 * <ul>
 *     <li>密码加密方式</li>
 *     <li>JWT 过滤器注册</li>
 *     <li>请求访问策略</li>
 *     <li>会话管理策略</li>
 * </ul>
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 密码加密器 Bean
     * <p>
     * 用于对用户密码进行加密和匹配校验。BCrypt 具有盐值随机性，安全性较高。
     * </p>
     *
     * @return BCryptPasswordEncoder 密码加密器
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT 认证过滤器 Bean
     * <p>
     * 该过滤器用于从请求头或其他位置获取 JWT Token，并进行验证、解析。
     * </p>
     *
     * @return JwtAuthenticationFilter 自定义 JWT 认证过滤器
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
    @Bean
    public AdminJwtAuthenticationFilter adminjwtAuthenticationFilter() {
        return new AdminJwtAuthenticationFilter();
    }

    /**
     * SecurityFilterChain - 核心安全过滤链配置
     * <p>
     * 主要配置内容：
     * <ul>
     *     <li>禁用 CSRF</li>
     *     <li>启用 CORS（若有需要可定制化配置）</li>
     *     <li>配置请求路径的访问权限</li>
     *     <li>会话管理策略设为无状态（STATELESS）</li>
     *     <li>注册 JWT 过滤器，优先级在 UsernamePasswordAuthenticationFilter 之前</li>
     * </ul>
     * </p>
     *
     * @param http HttpSecurity 对象，由 Spring 容器注入
     * @return SecurityFilterChain 过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 禁用 CSRF
        http.csrf(csrf -> csrf.disable());

        // 启用 CORS（如果需要可进一步配置 CORS 参数）
        http.cors(cors -> {
        });

        // 配置请求访问策略
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/user/auth/login", "/api/user/auth/register", "/api/user/auth/refresh-token").permitAll()
                .requestMatchers("/api/admin/auth/login").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/books", "/api/books/**", "/api/categories", "/api/categories/**", "/api/system/**").permitAll()
                .requestMatchers("/api/user/profile/avatar/**").permitAll()
                .requestMatchers("/api/admin/auth/logout", "/api/admin/dashboard/statistics", "/api/admin/users/**").authenticated()
                .requestMatchers("/api/user/auth/logout").authenticated()
                .requestMatchers("/api/user/behavior/**").authenticated()
                .anyRequest().authenticated()
        );

        // 配置异常处理：确保未认证请求返回 401 而非默认的 Access Denied (403)
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\": 401, \"message\": \"登录状态过期，请重新登录\"}");
                })
        );

        // 会话管理策略：使用无状态模式，所有认证信息通过 Token 传递
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 在 UsernamePasswordAuthenticationFilter 前注册自定义的 JWT 过滤器
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        // 在 UsernamePasswordAuthenticationFilter 前注册自定义的 JWT 过滤器
        http.addFilterBefore(adminjwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        // 构建并返回过滤器链
        return http.build();
    }
}
