package com.microservice.skeleton.user.interceptor;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userInfo = request.getHeader("UserInfo");
//        if (StringUtils.isEmpty(userInfo)) {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: No user info found");
//            return false;  // 拦截请求
//        }
        // 可以进一步解析 userInfo，检查其有效性，或者其他权限校验
        return true;
    }

}
