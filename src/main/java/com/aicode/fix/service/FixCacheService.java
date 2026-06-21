package com.aicode.fix.service;

import com.aicode.fix.model.FixSuggestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixCacheService {

    private static final String KEY_PREFIX = "fix:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redisTemplate;

    public FixSuggestion get(String ruleId, String contentHash) {
        if (contentHash == null || contentHash.isEmpty()) return null;
        String key = buildKey(ruleId, contentHash);
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof FixSuggestion suggestion) {
            log.debug("Fix 缓存命中: ruleId={}, hash={}", ruleId, contentHash.substring(0, 8));
            return suggestion;
        }
        return null;
    }

    public void put(String ruleId, String contentHash, FixSuggestion suggestion) {
        if (contentHash == null || contentHash.isEmpty() || suggestion == null) return;
        String key = buildKey(ruleId, contentHash);
        redisTemplate.opsForValue().set(key, suggestion, TTL);
    }

    private String buildKey(String ruleId, String contentHash) {
        return KEY_PREFIX + ruleId + ":" + contentHash;
    }
}
