package com.microservice.skeleton.user.config;

import com.google.common.collect.Maps;
import com.microservice.skeleton.user.filter.XssAndSqlFilter;
import com.microservice.skeleton.user.interceptor.AuthenticationInterceptor; // 导入拦截器
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // 导入接口

import java.util.Map;

/**
 * Web 配置类
 * 1. 注册拦截器 (解决串号问题)
 * 2. 注册 XSS/SQL 过滤器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer { // 👈 必须实现这个接口

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor; // 👈 注入刚才写的拦截器

    /**
     * 🔥 注册拦截器：这一步不写，拦截器就不会生效！
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/api/**") // 拦截所有API请求
                // 放行登录相关的接口，防止死循环
                .excludePathPatterns(
                        "/api/wechat/generate-auth-url",
                        "/api/wechat/exchange-code",
                        "/api/wechat/auth-callback",
                        "/api/wechat/js-sdk-config"
                );
    }


    @Bean
    public XssAndSqlFilter getXssAndSqlFilter() {
        return new XssAndSqlFilter();
    }

    @Bean
    public FilterRegistrationBean<XssAndSqlFilter> xssFilterRegistrationBean() {
        FilterRegistrationBean<XssAndSqlFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(getXssAndSqlFilter());
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/*");

        Map<String, String> initParameters = Maps.newHashMap();
        initParameters.put("excludes", "/favicon.ico,/img/*,/js/*,/css/*");
        initParameters.put("isIncludeRichText", "true");
        filterRegistrationBean.setInitParameters(initParameters);

        return filterRegistrationBean;
    }
}
