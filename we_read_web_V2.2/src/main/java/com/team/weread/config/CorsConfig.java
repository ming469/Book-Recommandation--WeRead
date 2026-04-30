package com.team.weread.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * 跨域资源共享 (CORS) 全局配置类。
 * <p>
 * 本类主要用于设置前端请求的跨域访问规则，主要配置内容包括：
 * <ul>
 *   <li>允许的请求路径</li>
 *   <li>允许的源地址</li>
 *   <li>允许的HTTP方法</li>
 *   <li>允许的请求头</li>
 *   <li>是否允许携带凭证（如cookies）</li>
 *   <li>预检请求的缓存时间</li>
 * </ul>
 * </p>
 */
@Configuration
public class CorsConfig {

    /*
     * 创建并返回一个用于配置全局跨域访问规则的 {@link WebMvcConfigurer} 实例。
     *
     * @return {@link WebMvcConfigurer} 实例，用于注册跨域配置
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * 配置跨域映射规则。
             *
             * @param registry 跨域资源共享注册表对象，用于注册具体的CORS规则
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")     // 对所有API路径生效
                        .allowedOriginPatterns("*")       // 允许所有源地址
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP请求方法
                        .allowedHeaders("*")              // 允许所有请求头
                        .allowCredentials(true)           // 允许携带凭证（如cookies）
                        .maxAge(3600);                    // 预检请求的缓存时间（秒）
            }
        };
    }
}