//package com.microservice.skeleton.user.config;
//
//import org.jasig.cas.client.session.SingleSignOutFilter;
//import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.cas.ServiceProperties;
//import org.springframework.security.cas.authentication.CasAuthenticationProvider;
//import org.springframework.security.cas.web.CasAuthenticationFilter;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.authentication.logout.LogoutFilter;
//import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
//
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Override
//    @Bean
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }
//    // 添加PasswordEncoder bean
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    private static final String CAS_SERVER_URL = "http://authserver.xmut.edu.cn/authserver";
//    private static final String CAS_SERVICE_URL = "http://localhost:8090";
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                .antMatchers("/", "/login**", "/logout**").permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .addFilter(casAuthenticationFilter())
//                .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class)
//                .logout()
//                .logoutSuccessUrl("/logout/cas")
//                .permitAll();
//    }
//
//    @Bean
//    public ServiceProperties serviceProperties() {
//        ServiceProperties sp = new ServiceProperties();
//        sp.setService(CAS_SERVICE_URL + "/login/cas");
//        sp.setSendRenew(false);
//        return sp;
//    }
//
//    @Bean
//    public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
//        CasAuthenticationFilter filter = new CasAuthenticationFilter();
//        filter.setAuthenticationManager(authenticationManager());
//        return filter;
//    }
//
//    @Bean
//    public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
//        return new Cas20ServiceTicketValidator(CAS_SERVER_URL);
//    }
//
//    @Bean
//    public CasAuthenticationProvider casAuthenticationProvider(
//            AuthenticationUserDetailsService authenticationUserDetailsService) {
//        CasAuthenticationProvider provider = new CasAuthenticationProvider();
//        provider.setServiceProperties(serviceProperties());
//        provider.setTicketValidator(cas20ServiceTicketValidator());
//        provider.setAuthenticationUserDetailsService(authenticationUserDetailsService);
//        provider.setKey("CAS_PROVIDER_LOCALHOST_8080");
//        return provider;
//    }
//
//    @Bean
//    public SingleSignOutFilter singleSignOutFilter() {
//        // 新版本简化的配置方式
//        return new SingleSignOutFilter();
//    }
//
//    @Bean
//    public LogoutFilter casLogoutFilter() {
//        return new LogoutFilter(
//                CAS_SERVER_URL + "/logout?service=" + CAS_SERVICE_URL,
//                new SecurityContextLogoutHandler());
//    }
//}


package com.microservice.skeleton.user.config;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String CAS_SERVER_URL = "http://authserver.xmut.edu.cn/authserver";
    private static final String CAS_SERVICE_URL = "http://localhost:8090";

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ 核心：开发阶段放行所有接口 + 跨域支持
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors() // 启用跨域
                .and()
                .csrf().disable() // 关闭 CSRF
                .authorizeRequests()
                .anyRequest().permitAll() // 放行所有接口
                .and()
                .logout()
                .logoutSuccessUrl("/logout/cas")
                .permitAll();

        // 注释掉以下内容可暂时禁用 CAS 过滤器，开发阶段不需要登录认证
        // .addFilter(casAuthenticationFilter())
        // .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class);
    }

    // ✅ 配置跨域允许所有来源、方法和请求头
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);  // 允许携带 cookie
        config.addAllowedOrigin("*");  // 允许所有来源
        config.addAllowedHeader("*");  // 允许所有请求头
        config.addAllowedMethod("*");  // 允许所有请求方法（GET, POST, PUT, DELETE）

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);  // 对所有接口启用跨域
        return source;
    }


    // 以下是 CAS 相关配置（如果你后期要启用，可以打开注释）
    @Bean
    public ServiceProperties serviceProperties() {
        ServiceProperties sp = new ServiceProperties();
        sp.setService(CAS_SERVICE_URL + "/login/cas");
        sp.setSendRenew(false);
        return sp;
    }

    @Bean
    public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
        CasAuthenticationFilter filter = new CasAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }

    @Bean
    public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
        return new Cas20ServiceTicketValidator(CAS_SERVER_URL);
    }

    @Bean
    public CasAuthenticationProvider casAuthenticationProvider(
            AuthenticationUserDetailsService authenticationUserDetailsService) {
        CasAuthenticationProvider provider = new CasAuthenticationProvider();
        provider.setServiceProperties(serviceProperties());
        provider.setTicketValidator(cas20ServiceTicketValidator());
        provider.setAuthenticationUserDetailsService(authenticationUserDetailsService);
        provider.setKey("CAS_PROVIDER_LOCALHOST_8080");
        return provider;
    }


    @Bean
    public LogoutFilter casLogoutFilter() {
        return new LogoutFilter(
                CAS_SERVER_URL + "/logout?service=" + CAS_SERVICE_URL,
                new SecurityContextLogoutHandler());
    }
}
