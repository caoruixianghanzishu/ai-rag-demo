package com.ai.demo.controller;

import com.ai.demo.dto.KnowledgeDocRequest;
import com.ai.demo.service.KnowledgeService;
import com.ai.demo.util.Result;
import com.ai.demo.vo.KnowledgeDocVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/add")
    public Result<Long> add(@Valid @RequestBody KnowledgeDocRequest request) {
        return Result.success(knowledgeService.add(request));
    }

    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody KnowledgeDocRequest request) {
        knowledgeService.update(request);
        return Result.success();
    }

    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        knowledgeService.delete(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<KnowledgeDocVO>> list() {
        return Result.success(knowledgeService.list());
    }

    @GetMapping("/detail")
    public Result<KnowledgeDocVO> detail(@RequestParam Long id) {
        return Result.success(knowledgeService.detail(id));
    }
}
