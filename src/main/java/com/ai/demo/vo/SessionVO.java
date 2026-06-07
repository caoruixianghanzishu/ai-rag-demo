package com.ai.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionVO {

    private Long id;

    private String sessionName;

    private LocalDateTime createTime;
}
