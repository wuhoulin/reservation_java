package com.microservice.skeleton.user.filter;

import jakarta.servlet.*; // 核心修改：javax -> jakarta
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * XSS 和 SQL 注入过滤器
 * 修改适配 Spring Boot 3 (Jakarta EE)
 */
@Component // 1. 交给 Spring 管理，不用再配置 @ServletComponentScan
@Order(1)  // 2. 设置优先级，数字越小越先执行
public class XssAndSqlFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化操作
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 检查请求是否是 HTTP 请求
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // 获取所有参数名
            String[] params = httpRequest.getParameterMap().keySet().toArray(new String[0]);
            for (String param : params) {
                String value = httpRequest.getParameter(param);
                if (containsXssOrSql(value)) {
                    // 如果发现恶意内容，返回 400 错误
                    httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal input detected (XSS/SQL injection risk).");
                    return; // ⛔️ 拦截请求，不再向下传递
                }
            }
        }

        // ✅ 安全，继续处理请求
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 清理工作
    }

    /**
     * 检查参数值是否包含 XSS 或 SQL 注入风险
     * ⚠️ 注意：这种简单的字符串包含判断容易产生误报（False Positive）
     */
    private boolean containsXssOrSql(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String lowerValue = value.toLowerCase();

        // XSS 简单检测 (建议生产环境使用 Jsoup 进行清洗)
        if (lowerValue.contains("<script>") ||
                lowerValue.contains("javascript:") ||
                lowerValue.contains("onerror=")) {
            return true;
        }


        // 示例：只拦截带空格的关键字，降低误报率
        if (lowerValue.contains(" select ") ||
                lowerValue.contains(" union ") ||
                lowerValue.contains(" drop table ") ||
                lowerValue.contains(" update ") && lowerValue.contains(" set ")) {
            return true;
        }

        // 你的原始逻辑（如果不介意误报，可以保留）
        // return lowerValue.contains("<script>") || lowerValue.contains("select") ...

        return false;
    }
}
