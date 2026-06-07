package com.ai.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContextMessage {

    private String role;

    private String content;

    public static ContextMessage from(Message message) {
        if (message instanceof UserMessage) {
            return new ContextMessage("user", message.getText());
        }
        if (message instanceof AssistantMessage) {
            return new ContextMessage("assistant", message.getText());
        }
        return new ContextMessage("user", message.getText());
    }

    public Message toMessage() {
        if ("assistant".equals(role)) {
            return new AssistantMessage(content);
        }
        return new UserMessage(content);
    }
}
