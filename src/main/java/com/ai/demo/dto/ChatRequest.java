package com.ai.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    /** 会话ID，为空则自动创建新会话 */
    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    private String message;
}
