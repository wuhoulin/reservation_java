package com.microservice.skeleton.user.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * XSS 和 SQL 注入过滤器
 */
@WebFilter(filterName = "XssAndSqlFilter", urlPatterns = "/*")
public class XssAndSqlFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 可以在这里进行初始化操作
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 检查请求是否是 HTTP 请求
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // 在这里可以进行 XSS 和 SQL 注入防护的处理
            // 比如清理请求参数中的潜在恶意内容
            String[] params = httpRequest.getParameterMap().keySet().toArray(new String[0]);
            for (String param : params) {
                String value = httpRequest.getParameter(param);
                if (containsXssOrSql(value)) {
                    // 如果发现恶意内容，给出警告或者处理
                    // 你可以在这里进行日志记录或拒绝请求
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "请求中包含不安全的内容！");
                    return;
                }
            }
        }

        // 继续处理请求
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 可以在这里进行清理工作
    }

    /**
     * 检查参数值是否包含 XSS 或 SQL 注入风险
     *
     * @param value 参数值
     * @return true 如果发现 XSS 或 SQL 注入攻击的潜在风险
     */
    private boolean containsXssOrSql(String value) {
        if (value == null) {
            return false;
        }

        // 这里可以增加更多 XSS 或 SQL 注入的检测逻辑
        String lowerValue = value.toLowerCase();
        return lowerValue.contains("<script>") || lowerValue.contains("select")
                || lowerValue.contains("drop") || lowerValue.contains("union");
    }
}
