package com.microservice.skeleton.user.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.microservice.skeleton.user.util.JwtTokenUtil;
import com.microservice.skeleton.user.util.UserContext;
import com.microservice.skeleton.user.controller.WeChatAuthController.WeChatUserInfo;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // 2. 验证并解析 Token
            if (jwtTokenUtil.validateToken(token)) {
                String openid = jwtTokenUtil.getOpenidFromToken(token);

                // 构造用户信息对象
                WeChatUserInfo userInfo = new WeChatUserInfo();
                userInfo.setOpenid(openid);

                // 🔥 进门：设置当前线程的用户
                UserContext.setCurrentUser(userInfo);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 🔥🔥🔥 核心修复点：出门必清理！🔥🔥🔥
        // 必须清空当前线程的 ThreadLocal，防止线程回到线程池后带脏数据
        UserContext.clear();
    }
}
