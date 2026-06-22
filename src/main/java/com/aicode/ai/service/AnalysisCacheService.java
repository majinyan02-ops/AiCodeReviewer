package com.aicode.ai.service;

import com.aicode.ai.model.AiIssueAnalysis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * AI 分析缓存服务 — 基于 Redis
 *
 * 基于方法体 contentHash 缓存 AI 分析结果。
 * 项目重新分析时，未变更的方法直接命中缓存，省 token。
 *
 * Key: ai:cache:{projectId}:{contentHash}
 * TTL: 30 天
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisCacheService {

    private static final String KEY_PREFIX = "ai:cache:";
    private static final Duration TTL = Duration.ofDays(30);

    private final RedisTemplate<String, Object> redisTemplate;

    public AiIssueAnalysis get(Long projectId, String contentHash) {
        if (contentHash == null || contentHash.isEmpty()) return null;
        String key = buildKey(projectId, contentHash);
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof AiIssueAnalysis analysis) {
            log.debug("缓存命中: projectId={}, hash={}", projectId, contentHash.substring(0, 8));
            return analysis;
        }
        return null;
    }

    public void put(Long projectId, String contentHash, AiIssueAnalysis analysis) {
        if (contentHash == null || contentHash.isEmpty() || analysis == null) return;
        String key = buildKey(projectId, contentHash);
        redisTemplate.opsForValue().set(key, analysis, TTL);
    }

    public void clear(Long projectId) {
        String pattern = KEY_PREFIX + projectId + ":*";
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            Long deleted = redisTemplate.delete(keys);
            log.info("清除项目缓存: projectId={}, 删除 {} 条", projectId, deleted);
        }
    }

    public long count(Long projectId) {
        String pattern = KEY_PREFIX + projectId + ":*";
        var keys = redisTemplate.keys(pattern);
        return keys != null ? keys.size() : 0;
    }

    private String buildKey(Long projectId, String contentHash) {
        return KEY_PREFIX + projectId + ":" + contentHash;
    }

    // ========== Review Agent 缓存 ==========

    private static final String REVIEW_KEY_PREFIX = "agent:review:";
    private static final Duration REVIEW_TTL = Duration.ofHours(24);

    /**
     * 获取 ReviewAgent 缓存结果
     */
    public Object getReviewResult(String projectId) {
        if (projectId == null || projectId.isEmpty()) return null;
        String key = REVIEW_KEY_PREFIX + projectId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 缓存 ReviewAgent 结果
     */
    public void putReviewResult(String projectId, Object result) {
        if (projectId == null || projectId.isEmpty() || result == null) return;
        String key = REVIEW_KEY_PREFIX + projectId;
        redisTemplate.opsForValue().set(key, result, REVIEW_TTL);
    }

    /**
     * 清除 ReviewAgent 缓存
     */
    public void clearReviewResult(String projectId) {
        if (projectId == null || projectId.isEmpty()) return;
        String key = REVIEW_KEY_PREFIX + projectId;
        redisTemplate.delete(key);
    }

    // ========== Fix Agent 缓存 ==========

    private static final String FIX_KEY_PREFIX = "agent:fix:";
    private static final Duration FIX_TTL = Duration.ofHours(24);

    /**
     * 获取 FixAgent 缓存结果
     */
    public Object getFixResult(String projectId) {
        if (projectId == null || projectId.isEmpty()) return null;
        String key = FIX_KEY_PREFIX + projectId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 缓存 FixAgent 结果
     */
    public void putFixResult(String projectId, Object result) {
        if (projectId == null || projectId.isEmpty() || result == null) return;
        String key = FIX_KEY_PREFIX + projectId;
        redisTemplate.opsForValue().set(key, result, FIX_TTL);
    }

    /**
     * 清除 FixAgent 缓存
     */
    public void clearFixResult(String projectId) {
        if (projectId == null || projectId.isEmpty()) return;
        String key = FIX_KEY_PREFIX + projectId;
        redisTemplate.delete(key);
    }
}
