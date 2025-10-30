package com.microservice.skeleton.user.domain.entity.User;

import lombok.Data;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;

@Data
public class CurrentUser {
    private String username;
    private String name; // 或其他自定义属性

    // 从 Principal 或 UserDetails 构造
    public CurrentUser(String username, String name) {
        this.username = username;
        this.name = name;
    }

    // 如果使用 Spring Security UserDetails
    public static CurrentUser from(UserDetails userDetails) {
        return new CurrentUser(userDetails.getUsername(), "未知"); // 可扩展
    }

    // 如果是 CAS 的 AttributePrincipal
    public static CurrentUser from(Principal principal) {
        if (principal instanceof AttributePrincipal) {
            AttributePrincipal casPrincipal = (AttributePrincipal) principal;
            String username = casPrincipal.getName();
            String name = (String) casPrincipal.getAttributes().get("cn");
            return new CurrentUser(username, name);
        }
        return new CurrentUser(principal.getName(), "未知");
    }
}
