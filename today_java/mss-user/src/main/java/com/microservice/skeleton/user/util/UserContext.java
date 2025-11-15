package com.microservice.skeleton.user.util;

import com.microservice.skeleton.user.controller.WeChatAuthController;

/**
 * 用户上下文工具类，用于在同一个线程中共享用户信息
 */
public class UserContext {

    private static final ThreadLocal<WeChatAuthController.WeChatUserInfo> USER_CONTEXT = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     */
    public static void setCurrentUser(WeChatAuthController.WeChatUserInfo userInfo) {
        USER_CONTEXT.set(userInfo);
    }

    /**
     * 获取当前用户信息
     */
    public static WeChatAuthController.WeChatUserInfo getCurrentUser() {
        return USER_CONTEXT.get();
    }

    /**
     * 获取当前用户的openid
     */
    public static String getCurrentOpenid() {
        WeChatAuthController.WeChatUserInfo userInfo = getCurrentUser();
        return userInfo != null ? userInfo.getOpenid() : null;
    }

    /**
     * 获取当前用户的accessToken
     */
    public static String getCurrentAccessToken() {
        WeChatAuthController.WeChatUserInfo userInfo = getCurrentUser();
        return userInfo != null ? userInfo.getAccessToken() : null;
    }

    /**
     * 清除用户上下文
     */
    public static void clear() {
        USER_CONTEXT.remove();
    }

    /**
     * 检查用户是否已登录
     */
    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }
}
