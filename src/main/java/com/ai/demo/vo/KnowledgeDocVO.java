package com.ai.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeDocVO {

    private Long id;

    private String title;

    private String content;

    private LocalDateTime createTime;
}
