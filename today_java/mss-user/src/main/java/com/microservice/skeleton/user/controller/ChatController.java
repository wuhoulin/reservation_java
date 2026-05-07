package com.microservice.skeleton.user.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatClient chatClient;
    private final StringRedisTemplate redisTemplate;

    public ChatController(ChatClient.Builder builder,
                          List<ToolCallback> tools,
                          StringRedisTemplate redisTemplate) {
        this.chatClient = builder
                .defaultAdvisors()
                .defaultFunctions(tools.toArray(new ToolCallback[0]))
                .build();
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/ai/route")
    public String planRoute(@RequestParam(value = "from") String from,
                            @RequestParam(value = "to") String to) {

        String cleanFrom = from.trim();
        String cleanTo = to.trim();

        String cacheKey = String.format("route:plan:%s:%s", cleanFrom, cleanTo);

        String cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cachedResult)) {
            return cachedResult;
        }

        String userMessage = String.format("帮我规划路线。起点坐标: %s，终点坐标: %s。同时查询终点的天气。", cleanFrom, cleanTo);

        String systemPrompt = """
            你是一个智能出行规划助手。
            
            【处理规则】
            输入均为"经度,纬度"格式的坐标。
            调用工具时请直接传入坐标参数，不要尝试将其转换为地名。
            必须调用工具查询：目的地天气、公交路线(transit)、驾车路线(driving)。
            
            【输出 JSON 格式要求】
            {
                "weather": { "summary": "天气简述", "tips": "出行建议" },
                "routes": [
                    {
                        "type": "打车/驾车",
                        "duration": "耗时",
                        "cost": "预估价格",
                        "details": "路线摘要",
                        "recommendation_score": 5,
                        // 核心：生成高德坐标导航链接
                        // 格式：https://uri.amap.com/navigation?from=起点坐标&to=终点坐标&mode=car&src=mypage
                        "link": "链接地址" 
                    },
                    {
                        "type": "公交/地铁",
                        "duration": "耗时",
                        "cost": "价格",
                        "details": "路线摘要",
                        "recommendation_score": 4,
                        "link": "链接地址(mode=bus)"
                    }
                ]
            }
            请只输出纯 JSON 字符串，不要 Markdown 标记。
            """;

        String aiResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        // 将 AI 响应存入 Redis，设置 15 分钟过期
        if (StringUtils.hasText(aiResponse)) {
            redisTemplate.opsForValue().set(cacheKey, aiResponse, 15, TimeUnit.MINUTES);
        }

        return aiResponse;
    }
}
