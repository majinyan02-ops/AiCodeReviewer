package com.aicode.patch.service;

import com.aicode.patch.model.PatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class PatchCacheService {

    private static final String KEY_PREFIX = "patch:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redisTemplate;

    public PatchResult get(String fixId) {
        if (fixId == null || fixId.isEmpty()) return null;
        String key = KEY_PREFIX + fixId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof PatchResult result) {
            log.debug("Patch 缓存命中: fixId={}", fixId);
            return result;
        }
        return null;
    }

    public void put(String fixId, PatchResult result) {
        if (fixId == null || fixId.isEmpty() || result == null) return;
        String key = KEY_PREFIX + fixId;
        redisTemplate.opsForValue().set(key, result, TTL);
    }
}
