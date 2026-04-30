package com.team.weread;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 微读应用程序主类
 * <p>
 * 应用程序的入口点，启用Spring Boot自动配置和组件扫描
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class WeReadApplication {

    /**
     * 应用程序主方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(WeReadApplication.class, args);
    }
}