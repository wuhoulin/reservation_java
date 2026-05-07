package com.microservice.skeleton.user.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeBaseService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public KnowledgeBaseService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 1. 知识录入：读取文件 -> 切片 -> 向量化 -> 存入 Redis
     */
    public String ingest(Resource resource) {
        TextReader textReader = new TextReader(resource);
        List<Document> documents = textReader.get();

        // 将长文本切分为小块 (Chunk)
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> splitDocuments = splitter.apply(documents);

        // 自动调用通义千问 Embedding 模型转为向量并存入 Redis
        vectorStore.add(splitDocuments);

        return "文档解析完成，共存入 Redis " + splitDocuments.size() + " 个知识段落。";
    }

    /**
     * 2. RAG 问答：接收问题 -> 检索 Redis -> 发给大模型
     */
    public String chat(String question) {
        return chatClient.prompt()
                .user(question)
                // 【注意这里！】换成了最新的 Builder 写法，绝对不会再报错了
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().topK(3).build()))
                .call()
                .content();
    }
}
