package com.ai.demo.service.impl;

import com.ai.demo.entity.AiChatMessage;
import com.ai.demo.entity.AiChatSession;
import com.ai.demo.mapper.AiChatMessageMapper;
import com.ai.demo.mapper.AiChatSessionMapper;
import com.ai.demo.service.SessionService;
import com.ai.demo.vo.MessageVO;
import com.ai.demo.vo.SessionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;

    @Override
    public List<SessionVO> listSessions() {
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AiChatSession::getCreateTime);
        return sessionMapper.selectList(wrapper).stream()
                .map(this::toSessionVO)
                .toList();
    }

    @Override
    public List<MessageVO> getHistory(Long sessionId) {
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, sessionId)
                .orderByAsc(AiChatMessage::getCreateTime);
        return messageMapper.selectList(wrapper).stream()
                .map(this::toMessageVO)
                .toList();
    }

    @Override
    public Long createSession(String sessionName) {
        AiChatSession session = new AiChatSession();
        session.setSessionName(sessionName);
        session.setCreateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session.getId();
    }

    private SessionVO toSessionVO(AiChatSession session) {
        SessionVO vo = new SessionVO();
        BeanUtils.copyProperties(session, vo);
        return vo;
    }

    private MessageVO toMessageVO(AiChatMessage message) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);
        return vo;
    }
}
