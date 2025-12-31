package com.microservice.skeleton.user.interceptor;

import com.microservice.skeleton.user.controller.WeChatAuthController.WeChatUserInfo;
import com.microservice.skeleton.user.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;



/**
 * 用户上下文拦截器
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从Session中获取用户信息
        WeChatUserInfo userInfo = (WeChatUserInfo) request.getSession().getAttribute("currentUser");

        if (userInfo != null) {
            // 将用户信息设置到ThreadLocal上下文
            UserContext.setCurrentUser(userInfo);
            System.out.println("拦截器设置用户信息到ThreadLocal: " + userInfo.getOpenid());
        } else {
            System.out.println("拦截器未找到Session中的用户信息");
        }

        return true;
    }


}
