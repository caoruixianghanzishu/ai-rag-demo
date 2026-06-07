package com.ai.demo.service;

import com.ai.demo.vo.MessageVO;
import com.ai.demo.vo.SessionVO;

import java.util.List;

public interface SessionService {

    List<SessionVO> listSessions();

    List<MessageVO> getHistory(Long sessionId);

    Long createSession(String sessionName);
}
