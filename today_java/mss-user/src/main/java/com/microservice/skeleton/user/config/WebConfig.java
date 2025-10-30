package com.microservice.skeleton.user.config;

import com.google.common.collect.Maps;
import com.microservice.skeleton.user.filter.XssAndSqlFilter;
import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

/**
 * Web 配置类，主要配置与 CAS（中央认证服务）相关的过滤器，
 * 并包括 XSS 和 SQL 注入过滤器的配置。
 */
@Configuration
public class WebConfig {

    /**
     * 配置 CAS 认证过滤器
     */
    @Bean
    public AuthenticationFilter getAuthenticationFilter() {
        return new AuthenticationFilter();
    }

    /**
     * 注册 CAS 认证过滤器
     */
    @Bean
    public FilterRegistrationBean authenticationFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(getAuthenticationFilter());
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/user/loginByCAS");
        filterRegistrationBean.setName("CASFilter");

        Map<String, String> initParameters = Maps.newHashMap();
        initParameters.put("casServerLoginUrl", "http://authserver.ndnu.edu.cn/authserver/login");
        initParameters.put("serverName", "http://43.139.169.190:9071/");
        filterRegistrationBean.setInitParameters(initParameters);

        return filterRegistrationBean;
    }

    /**
     * 配置 CAS 票据验证过滤器
     */
    @Bean
    public Cas20ProxyReceivingTicketValidationFilter getCas20ProxyReceivingTicketValidationFilter() {
        return new Cas20ProxyReceivingTicketValidationFilter();
    }

    /**
     * 注册 CAS 票据验证过滤器
     */
    @Bean
    public FilterRegistrationBean cas20ProxyReceivingTicketValidationFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(getCas20ProxyReceivingTicketValidationFilter());
        filterRegistrationBean.setName("CAS Validation Filter");
        filterRegistrationBean.setOrder(2);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/user/loginByCAS");

        Map<String, String> initParameters = Maps.newHashMap();
        initParameters.put("encoding", "UTF-8");
        initParameters.put("casServerUrlPrefix", "http://authserver.ndnu.edu.cn/authserver");
        initParameters.put("serverName", "http://210.34.24.34:40080");
        filterRegistrationBean.setInitParameters(initParameters);

        return filterRegistrationBean;
    }

    /**
     * 配置 HTTP 请求包装过滤器
     */
    @Bean
    public HttpServletRequestWrapperFilter getHttpServletRequestWrapperFilter() {
        return new HttpServletRequestWrapperFilter();
    }

    /**
     * 注册 HTTP 请求包装过滤器
     */
    @Bean
    public FilterRegistrationBean httpServletRequestWrapperFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(getHttpServletRequestWrapperFilter());
        filterRegistrationBean.setName("CAS HttpServletRequest Wrapper Filter");
        filterRegistrationBean.setOrder(3);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/*");

        return filterRegistrationBean;
    }

    /**
     * 配置 CAS 单点注销过滤器
     */
    @Bean
    public SingleSignOutFilter getSingleSignOutFilter() {
        return new SingleSignOutFilter();
    }

    /**
     * 注册 CAS 单点注销过滤器
     */
    @Bean
    public FilterRegistrationBean singleSignOutFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(getSingleSignOutFilter());
        filterRegistrationBean.setName("CAS Single Sign Out Filter");
        filterRegistrationBean.setOrder(4);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/*");

        return filterRegistrationBean;
    }

    /**
     * 配置 XSS 和 SQL 注入过滤器
     */
    @Bean
    public XssAndSqlFilter getXssAndSqlFilter() {
        return new XssAndSqlFilter();
    }

    /**
     * 注册 XSS 和 SQL 注入过滤器
     */
    @Bean
    public FilterRegistrationBean xssFilterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
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
