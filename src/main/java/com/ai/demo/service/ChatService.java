package com.ai.demo.service;

import com.ai.demo.dto.ChatRequest;
import com.ai.demo.vo.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {

    ChatResponse chat(ChatRequest request);

    Flux<String> streamChat(Long sessionId, String message);

    List<Message> getContextMessages(Long sessionId);

    void saveContextMessage(Long sessionId, Message message);
}
