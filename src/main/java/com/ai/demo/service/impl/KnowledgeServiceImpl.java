package com.ai.demo.service.impl;

import com.ai.demo.dto.KnowledgeDocRequest;
import com.ai.demo.entity.KnowledgeDoc;
import com.ai.demo.mapper.KnowledgeDocMapper;
import com.ai.demo.service.KnowledgeService;
import com.ai.demo.vo.KnowledgeDocVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeDocMapper knowledgeDocMapper;

    @Override
    public Long add(KnowledgeDocRequest request) {
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle(request.getTitle());
        doc.setContent(request.getContent());
        doc.setCreateTime(LocalDateTime.now());
        knowledgeDocMapper.insert(doc);
        return doc.getId();
    }

    @Override
    public void update(KnowledgeDocRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("文档ID不能为空");
        }
        KnowledgeDoc doc = knowledgeDocMapper.selectById(request.getId());
        if (doc == null) {
            throw new IllegalArgumentException("知识库文档不存在");
        }
        doc.setTitle(request.getTitle());
        doc.setContent(request.getContent());
        knowledgeDocMapper.updateById(doc);
    }

    @Override
    public void delete(Long id) {
        knowledgeDocMapper.deleteById(id);
    }

    @Override
    public List<KnowledgeDocVO> list() {
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(KnowledgeDoc::getCreateTime);
        return knowledgeDocMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public KnowledgeDocVO detail(Long id) {
        KnowledgeDoc doc = knowledgeDocMapper.selectById(id);
        if (doc == null) {
            throw new IllegalArgumentException("知识库文档不存在");
        }
        return toVO(doc);
    }

    private KnowledgeDocVO toVO(KnowledgeDoc doc) {
        KnowledgeDocVO vo = new KnowledgeDocVO();
        BeanUtils.copyProperties(doc, vo);
        return vo;
    }
}
