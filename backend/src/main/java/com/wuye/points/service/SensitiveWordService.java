package com.wuye.points.service;

import com.wuye.points.entity.SensitiveWord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.SensitiveWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 敏感词过滤：对发帖/评论内容做包含匹配，命中即拦截。
 * 词库来自 sensitive_words 表，规模较小，直接全量加载匹配。
 */
@Service
@RequiredArgsConstructor
public class SensitiveWordService {

    private final SensitiveWordRepository sensitiveWordRepository;

    public void assertNotSensitive(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        List<SensitiveWord> words = sensitiveWordRepository.findAll();
        for (SensitiveWord word : words) {
            if (word.getWord() != null && !word.getWord().isEmpty() && text.contains(word.getWord())) {
                throw new BizException(ErrorCode.COMMUNITY_FORBIDDEN_WORD);
            }
        }
    }
}
