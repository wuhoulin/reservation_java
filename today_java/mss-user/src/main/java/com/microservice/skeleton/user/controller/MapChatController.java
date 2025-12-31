//package com.microservice.skeleton.user.controller;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class MapChatController {
//
//    private final ChatClient chatClient;
//
//    // 使用构造器注入
//    public MapChatController(ChatClient.Builder builder) {
//        this.chatClient = builder
//                // 在 M6 版本中，如果你正确配置了 MCP Starter，
//                // 工具通常会自动注册，或者你可以加上 .defaultTools()
//                // 如果运行后发现 AI 不调地图，回来这里加 .defaultTools(toolCallbackProvider)
//                .build();
//    }
//
//    @GetMapping("/ask-map")
//    public String askMap(@RequestParam String question) {
//        return chatClient.prompt()
//                .user(question)
//                .call()
//                .content();
//    }
//}
