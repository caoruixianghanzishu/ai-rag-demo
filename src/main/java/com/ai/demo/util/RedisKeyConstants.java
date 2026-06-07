package com.ai.demo.util;

public class RedisKeyConstants {

    public static final String CHAT_CONTEXT_PREFIX = "chat:context:";

    /** 上下文消息保留条数上限 */
    public static final int MAX_CONTEXT_MESSAGES = 20;

    private RedisKeyConstants() {
    }

    public static String chatContextKey(Long sessionId) {
        return CHAT_CONTEXT_PREFIX + sessionId;
    }
}
