package com.microservice.skeleton.user.Manager;


import com.microservice.skeleton.user.domain.entity.Token;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenManager {

    private Map<String, Token> tokenStore = new ConcurrentHashMap<>();

    // 存 token（你可以改成用 Redis 存储）
    public void set(String tokenValue, Token token) {
        tokenStore.put(tokenValue, token);
    }

    // 取 token
    public Token get(String tokenValue) {
        return tokenStore.get(tokenValue);
    }

    // 删 token
    public void deleteToken(Long userId, String tokenValue) {
        tokenStore.remove(tokenValue);
    }
}
