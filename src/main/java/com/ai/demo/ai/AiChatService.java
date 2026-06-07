package com.ai.demo.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AiChatService {

    private final ChatClient chatClient;

    /**
     * 单轮对话（带 RAG Prompt）
     */
    public String chat(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 多轮对话
     */
    public String chatWithHistory(List<Message> history, String userMessage) {
        List<Message> messages = new ArrayList<>(history);
        messages.add(new UserMessage(userMessage));

        return chatClient.prompt()
                .messages(messages)
                .call()
                .content();
    }

    /**
     * 流式输出
     */
    public Flux<String> streamChat(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }

    /**
     * 流式多轮对话
     */
    public Flux<String> streamChatWithHistory(List<Message> history, String userMessage) {
        List<Message> messages = new ArrayList<>(history);
        messages.add(new UserMessage(userMessage));

        return chatClient.prompt()
                .messages(messages)
                .stream()
                .content();
    }

    public UserMessage toUserMessage(String content) {
        return new UserMessage(content);
    }

    public AssistantMessage toAssistantMessage(String content) {
        return new AssistantMessage(content);
    }
}
