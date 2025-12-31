package com.microservice.skeleton.user.config;

import com.google.common.collect.Maps;
import com.microservice.skeleton.user.filter.XssAndSqlFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Web 配置类
 * 已移除 CAS 相关配置，仅保留 XSS 和 SQL 注入过滤器
 */
@Configuration
public class WebConfig {

    /**
     * 配置 XSS 和 SQL 注入过滤器
     * 确保你的 XssAndSqlFilter 已经升级为使用 jakarta.servlet.Filter
     */
    @Bean
    public XssAndSqlFilter getXssAndSqlFilter() {
        return new XssAndSqlFilter();
    }

    /**
     * 注册 XSS 和 SQL 注入过滤器
     */
    @Bean
    public FilterRegistrationBean<XssAndSqlFilter> xssFilterRegistrationBean() {
        FilterRegistrationBean<XssAndSqlFilter> filterRegistrationBean = new FilterRegistrationBean<>();

        // 关键点：getXssAndSqlFilter() 返回的类必须实现 jakarta.servlet.Filter
        filterRegistrationBean.setFilter(getXssAndSqlFilter());

        filterRegistrationBean.setOrder(1); // 设置最高优先级，最先过滤不安全内容
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/*");

        // 设置初始化参数
        Map<String, String> initParameters = Maps.newHashMap();
        initParameters.put("excludes", "/favicon.ico,/img/*,/js/*,/css/*");
        initParameters.put("isIncludeRichText", "true");
        filterRegistrationBean.setInitParameters(initParameters);

        return filterRegistrationBean;
    }
}
