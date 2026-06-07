package com.ai.demo.rag;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    private static final String RAG_TEMPLATE = """
            你是一名高校学工系统智能助手。
            请根据以下知识库内容回答问题。
            用户可能用不同说法提问（如"怎么选课"与"选课规则"），请理解语义后作答。

            知识库内容：
            %s

            用户问题：
            %s

            如果知识库没有相关内容，请明确说明未找到相关资料。
            """;

    public String buildRagPrompt(String knowledgeContent, String question) {
        return String.format(RAG_TEMPLATE, knowledgeContent, question);
    }
}
