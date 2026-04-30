package com.team.weread.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 * <p>
 * 提供JWT令牌的生成、解析和验证功能
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@Component
public class JwtUtil {

    /**
     * JWT密钥，从配置文件中读取
     */
    private static String secret;

    /**
     * JWT有效期（毫秒），默认24小时
     */
    private static long expiration;

    /**
     * 记住我时的JWT有效期（毫秒），默认7天
     */
    private static long rememberMeExpiration;

    @Value("${jwt.secret:defaultSecretKeyWhichShouldBeVeryLongAndSecure}")
    public void setSecret(String secret) {
        JwtUtil.secret = secret;
    }

    @Value("${jwt.expiration:86400000}")
    public void setExpiration(long expiration) {
        JwtUtil.expiration = expiration;
    }

    @Value("${jwt.remember-me-expiration:604800000}")
    public void setRememberMeExpiration(long rememberMeExpiration) {
        JwtUtil.rememberMeExpiration = rememberMeExpiration;
    }

    /**
     * 从JWT中提取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public static String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从JWT中提取过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public static Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从JWT中提取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public static Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从JWT中提取指定的声明
     *
     * @param token          JWT令牌
     * @param claimsResolver 声明解析函数
     * @param <T>            声明类型
     * @return 声明值
     */
    public static <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析JWT获取所有声明
     *
     * @param token JWT令牌
     * @return 所有声明
     */
    private static Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 如果令牌过期，依然返回其中的声明信息
            return e.getClaims();
        }
    }

    /**
     * 检查JWT是否过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    private static Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 为用户生成JWT
     *
     * @param username   用户名
     * @param userId     用户ID
     * @param rememberMe 是否记住登录状态
     * @return JWT令牌
     */
    public static String generateToken(String username, Long userId, Boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims, username, rememberMe ? rememberMeExpiration : expiration);
    }

    /**
     * 创建JWT
     *
     * @param claims     声明
     * @param subject    主题（通常是用户名）
     * @param expiration 有效期（毫秒）
     * @return JWT令牌
     */
    private static String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证JWT
     *
     * @param token    JWT令牌
     * @param username 用户名
     * @return 是否有效
     */
    public static Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * 获取签名密钥
     *
     * @return 签名密钥
     */
    private static Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
