package com.ai.demo.controller;

import com.ai.demo.service.SessionService;
import com.ai.demo.util.Result;
import com.ai.demo.vo.MessageVO;
import com.ai.demo.vo.SessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/list")
    public Result<List<SessionVO>> list() {
        return Result.success(sessionService.listSessions());
    }

    @GetMapping("/history")
    public Result<List<MessageVO>> history(@RequestParam Long sessionId) {
        return Result.success(sessionService.getHistory(sessionId));
    }
}
