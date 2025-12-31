package com.microservice.skeleton.user.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback; // 使用通用的 ToolCallback
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collection;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder, ApplicationContext context) {
        // 查找 Spring 容器中所有的工具回调（包括 MCP 自动生成的）
        Collection<ToolCallback> toolCallbacks = context
                .getBeansOfType(ToolCallback.class)
                .values();

        this.chatClient = builder
                .defaultAdvisors()
                // 直接将所有工具注册进去
                .defaultFunctions(toolCallbacks.toArray(new ToolCallback[0]))
                .build();
    }

    @GetMapping("/ai/chat")
    public String chat(@RequestParam(value = "message", defaultValue = "你好") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
