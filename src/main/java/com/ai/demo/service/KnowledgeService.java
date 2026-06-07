package com.ai.demo.service;

import com.ai.demo.dto.KnowledgeDocRequest;
import com.ai.demo.vo.KnowledgeDocVO;

import java.util.List;

public interface KnowledgeService {

    Long add(KnowledgeDocRequest request);

    void update(KnowledgeDocRequest request);

    void delete(Long id);

    List<KnowledgeDocVO> list();

    KnowledgeDocVO detail(Long id);
}
