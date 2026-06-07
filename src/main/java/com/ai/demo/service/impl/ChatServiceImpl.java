package com.ai.demo.service.impl;

import com.ai.demo.ai.AiChatService;
import com.ai.demo.dto.ChatRequest;
import com.ai.demo.dto.ContextMessage;
import com.ai.demo.entity.AiChatMessage;
import com.ai.demo.entity.AiChatSession;
import com.ai.demo.mapper.AiChatMessageMapper;
import com.ai.demo.mapper.AiChatSessionMapper;
import com.ai.demo.rag.RagService;
import com.ai.demo.service.ChatService;
import com.ai.demo.util.RedisKeyConstants;
import com.ai.demo.vo.ChatResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final AiChatService aiChatService;
    private final RagService ragService;
    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public ChatResponse chat(ChatRequest request) {
        Long sessionId = resolveSessionId(request.getSessionId(), request.getMessage());
        String userMessage = request.getMessage();

        saveMessage(sessionId, "user", userMessage);
        saveContextMessage(sessionId, new UserMessage(userMessage));

        List<Message> history = getContextMessages(sessionId);
        String ragPrompt = ragService.buildPromptWithKnowledge(userMessage);

        String answer;
        if (history.size() <= 1) {
            answer = aiChatService.chat(ragPrompt);
        } else {
            answer = aiChatService.chatWithHistory(
                    history.subList(0, history.size() - 1), ragPrompt);
        }

        saveMessage(sessionId, "assistant", answer);
        saveContextMessage(sessionId, new AssistantMessage(answer));

        return new ChatResponse(sessionId, answer);
    }

    @Override
    public Flux<String> streamChat(Long sessionId, String message) {
        if (!StringUtils.hasText(message)) {
            return Flux.error(new IllegalArgumentException("消息内容不能为空"));
        }

        Long resolvedSessionId = resolveSessionId(sessionId, message);
        String userMessage = message;

        saveMessage(resolvedSessionId, "user", userMessage);
        saveContextMessage(resolvedSessionId, new UserMessage(userMessage));

        List<Message> history = getContextMessages(resolvedSessionId);
        String ragPrompt = ragService.buildPromptWithKnowledge(userMessage);

        Flux<String> flux;
        if (history.size() <= 1) {
            flux = aiChatService.streamChat(ragPrompt);
        } else {
            flux = aiChatService.streamChatWithHistory(
                    history.subList(0, history.size() - 1), ragPrompt);
        }

        StringBuilder fullAnswer = new StringBuilder();
        return flux.doOnNext(fullAnswer::append)
                .doOnComplete(() -> {
                    saveMessage(resolvedSessionId, "assistant", fullAnswer.toString());
                    saveContextMessage(resolvedSessionId, new AssistantMessage(fullAnswer.toString()));
                });
    }

    @Override
    public List<Message> getContextMessages(Long sessionId) {
        String key = RedisKeyConstants.chatContextKey(sessionId);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof List<?> list && !list.isEmpty()) {
            List<Message> messages = new ArrayList<>();
            for (Object item : list) {
                ContextMessage contextMessage = toContextMessage(item);
                if (contextMessage != null) {
                    messages.add(contextMessage.toMessage());
                }
            }
            if (!messages.isEmpty()) {
                return messages;
            }
        }
        return loadContextFromDb(sessionId);
    }

    @Override
    public void saveContextMessage(Long sessionId, Message message) {
        List<ContextMessage> messages = new ArrayList<>();
        for (Message existing : getContextMessages(sessionId)) {
            messages.add(ContextMessage.from(existing));
        }
        messages.add(ContextMessage.from(message));

        if (messages.size() > RedisKeyConstants.MAX_CONTEXT_MESSAGES) {
            messages = new ArrayList<>(messages.subList(
                    messages.size() - RedisKeyConstants.MAX_CONTEXT_MESSAGES, messages.size()));
        }

        String key = RedisKeyConstants.chatContextKey(sessionId);
        redisTemplate.opsForValue().set(key, messages, 24, TimeUnit.HOURS);
    }

    private Long resolveSessionId(Long sessionId, String firstMessage) {
        if (sessionId != null) {
            AiChatSession session = sessionMapper.selectById(sessionId);
            if (session != null) {
                return sessionId;
            }
        }
        AiChatSession session = new AiChatSession();
        String name = firstMessage.length() > 20 ? firstMessage.substring(0, 20) + "..." : firstMessage;
        session.setSessionName(name);
        session.setCreateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session.getId();
    }

    private void saveMessage(Long sessionId, String role, String content) {
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }

    private List<Message> loadContextFromDb(Long sessionId) {
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, sessionId)
                .orderByAsc(AiChatMessage::getCreateTime)
                .last("LIMIT " + RedisKeyConstants.MAX_CONTEXT_MESSAGES);

        List<AiChatMessage> dbMessages = messageMapper.selectList(wrapper);
        List<ContextMessage> contextMessages = new ArrayList<>();
        List<Message> messages = new ArrayList<>();
        for (AiChatMessage msg : dbMessages) {
            contextMessages.add(new ContextMessage(msg.getRole(), msg.getContent()));
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        if (!contextMessages.isEmpty()) {
            String key = RedisKeyConstants.chatContextKey(sessionId);
            redisTemplate.opsForValue().set(key, contextMessages, 24, TimeUnit.HOURS);
        }
        return messages;
    }

    private ContextMessage toContextMessage(Object item) {
        if (item instanceof ContextMessage contextMessage) {
            return contextMessage;
        }
        if (item instanceof Map<?, ?> map) {
            return objectMapper.convertValue(map, ContextMessage.class);
        }
        return null;
    }
}
