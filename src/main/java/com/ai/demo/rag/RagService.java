package com.ai.demo.rag;

import com.ai.demo.entity.KnowledgeDoc;
import com.ai.demo.mapper.KnowledgeDocMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagService {

    private static final int MIN_KEYWORD_LENGTH = 2;
    private static final int MIN_SUBSTRING_LENGTH = 2;

    private final KnowledgeDocMapper knowledgeDocMapper;
    private final PromptBuilder promptBuilder;

    /**
     * 简易 RAG：通过关键词模糊匹配知识库，拼接 Prompt
     */
    public String buildPromptWithKnowledge(String question) {
        List<KnowledgeDoc> docs = searchKnowledge(question);
        if (docs.isEmpty()) {
            return promptBuilder.buildRagPrompt("（暂无匹配内容）", question);
        }
        String content = docs.stream()
                .map(doc -> "【" + doc.getTitle() + "】\n" + doc.getContent())
                .collect(Collectors.joining("\n\n"));
        return promptBuilder.buildRagPrompt(content, question);
    }

    public List<KnowledgeDoc> searchKnowledge(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        List<String> keywords = extractKeywords(keyword);
        List<KnowledgeDoc> allDocs = knowledgeDocMapper.selectList(
                new LambdaQueryWrapper<KnowledgeDoc>().orderByDesc(KnowledgeDoc::getCreateTime));

        return allDocs.stream()
                .map(doc -> new ScoredDoc(doc, score(doc, keywords)))
                .filter(scored -> scored.score > 0)
                .sorted(Comparator.comparingInt(ScoredDoc::score).reversed())
                .limit(5)
                .map(scored -> scored.doc)
                .toList();
    }

    private int score(KnowledgeDoc doc, List<String> keywords) {
        int maxScore = 0;
        for (String kw : keywords) {
            if (doc.getTitle().equals(kw)) {
                maxScore = Math.max(maxScore, 100);
            } else if (doc.getTitle().contains(kw)) {
                maxScore = Math.max(maxScore, 80);
            } else if (kw.contains(doc.getTitle())) {
                maxScore = Math.max(maxScore, 70);
            } else if (doc.getContent().contains(kw)) {
                maxScore = Math.max(maxScore, 60);
            } else if (hasCommonSubstring(kw, doc.getTitle()) || hasCommonSubstring(kw, doc.getContent())) {
                maxScore = Math.max(maxScore, 40);
            }
        }
        return maxScore;
    }

    /** 提取检索关键词：去掉疑问词后，保留核心语义 */
    private List<String> extractKeywords(String keyword) {
        Set<String> keywords = new LinkedHashSet<>();
        String query = normalizeKeyword(keyword);

        if (StringUtils.hasText(query)) {
            keywords.add(query);
        }

        // 去掉句首疑问词：怎么选课 -> 选课
        String withoutPrefix = query.replaceFirst("^(怎么|如何|怎样|为啥|为什么|什么是|请问|我想知道|告诉我|能不能|是否)", "");
        if (StringUtils.hasText(withoutPrefix) && withoutPrefix.length() >= MIN_KEYWORD_LENGTH) {
            keywords.add(withoutPrefix);
        }

        // 去掉句尾疑问词：选课是什么 -> 选课
        String core = withoutPrefix.replaceFirst("(是什么|有哪些|有什么|什么意思|怎么办|怎么做|可以吗|可以吗|吗|呢|啊)$", "");
        if (StringUtils.hasText(core) && core.length() >= MIN_KEYWORD_LENGTH) {
            keywords.add(core);
        }

        return new ArrayList<>(keywords);
    }

    private boolean hasCommonSubstring(String query, String text) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(query)) {
            return false;
        }
        int maxLen = Math.min(text.length(), 8);
        for (int len = maxLen; len >= MIN_SUBSTRING_LENGTH; len--) {
            for (int i = 0; i <= text.length() - len; i++) {
                String part = text.substring(i, i + len);
                if (query.contains(part)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalizeKeyword(String keyword) {
        return keyword.replaceAll("[？?。！!，,、\\s]", "");
    }

    private record ScoredDoc(KnowledgeDoc doc, int score) {}
}
