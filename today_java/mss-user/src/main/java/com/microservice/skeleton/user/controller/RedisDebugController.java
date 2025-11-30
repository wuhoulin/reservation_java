package com.microservice.skeleton.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/debug")
public class RedisDebugController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查Redis状态和数据
     */
    @GetMapping("/redis-status")
    public Map<String, Object> checkRedisStatus() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查Redis连接
            String pingResult = redisTemplate.getConnectionFactory().getConnection().ping();
            result.put("redisConnection", "OK");
            result.put("ping", pingResult);

            // 获取所有key
            Set<String> keys = redisTemplate.keys("*");
            result.put("totalKeys", keys.size());
            result.put("keys", keys);

            // 统计不同类型的key
            Map<String, Integer> keyTypeCount = new HashMap<>();
            Map<String, Object> keyDetails = new HashMap<>();

            for (String key : keys) {
                // 获取key类型
                DataType type = redisTemplate.type(key);
                String typeName = type != null ? type.code() : "unknown";

                keyTypeCount.put(typeName, keyTypeCount.getOrDefault(typeName, 0) + 1);

                // 获取key的值（只显示前几个字符避免数据过大）
                Object value = getKeyValue(key, type);
                keyDetails.put(key, value);
            }

            result.put("keyTypes", keyTypeCount);
            result.put("keyDetails", keyDetails);

        } catch (Exception e) {
            result.put("redisConnection", "ERROR");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 检查特定用户数据
     */
    @GetMapping("/user-data/{openid}")
    public Map<String, Object> checkUserData(@PathVariable String openid) {
        Map<String, Object> result = new HashMap<>();

        String userKey = "wechat:user:" + openid;
        String tokenKey = "wechat:token:" + openid;
        String sessionKey = "wechat:session:" + openid;

        result.put("userKey", userKey);
        result.put("userData", redisTemplate.opsForValue().get(userKey));
        result.put("tokenKey", tokenKey);
        result.put("tokenData", redisTemplate.opsForValue().get(tokenKey));
        result.put("sessionKey", sessionKey);
        result.put("sessionData", redisTemplate.opsForValue().get(sessionKey));

        // 检查key是否存在
        result.put("userKeyExists", redisTemplate.hasKey(userKey));
        result.put("tokenKeyExists", redisTemplate.hasKey(tokenKey));
        result.put("sessionKeyExists", redisTemplate.hasKey(sessionKey));

        return result;
    }

    /**
     * 清空Redis数据（谨慎使用）
     */
    @PostMapping("/clear-redis")
    public Map<String, Object> clearRedis() {
        Map<String, Object> result = new HashMap<>();

        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            result.put("success", true);
            result.put("message", "Redis数据已清空");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    private Object getKeyValue(String key, DataType type) {
        try {
            if (type == DataType.STRING) {
                Object value = redisTemplate.opsForValue().get(key);
                if (value instanceof String) {
                    String strValue = (String) value;
                    // 只返回前100个字符
                    return strValue.length() > 100 ? strValue.substring(0, 100) + "..." : strValue;
                }
                return value;
            } else if (type == DataType.HASH) {
                return redisTemplate.opsForHash().entries(key);
            } else if (type == DataType.LIST) {
                return redisTemplate.opsForList().range(key, 0, 4); // 前5个元素
            } else if (type == DataType.SET) {
                return redisTemplate.opsForSet().members(key);
            } else if (type == DataType.ZSET) {
                return redisTemplate.opsForZSet().range(key, 0, 4); // 前5个元素
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        return "Unsupported type";
    }
}
