package com.microservice.skeleton.user.config;

import com.microservice.skeleton.user.interceptor.UserContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserContextInterceptor userContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/api/**") // 拦截所有API请求
                .excludePathPatterns("/api/wechat/generate-auth-url") // 排除授权URL生成
                .excludePathPatterns("/api/wechat/exchange-code");   // 排除code兑换
    }
}
