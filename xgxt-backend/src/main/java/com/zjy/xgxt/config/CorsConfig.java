package com.zjy.xgxt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 告诉 Spring Boot 这是一个配置类，启动时会自动加载
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路径
        registry.addMapping("/**")
                // 设置允许跨域请求的域名 (使用 allowedOriginPatterns 而不是 allowedOrigins 兼容性更好)
                .allowedOriginPatterns("*")
                // 是否允许证书 (如 Cookie、Token)
                .allowCredentials(true)
                // 设置允许的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 设置允许的 header 属性
                .allowedHeaders("*")
                // 跨域允许时间 (秒)，在此时间内浏览器不需要再发 OPTIONS 预检请求
                .maxAge(3600);
    }
}
