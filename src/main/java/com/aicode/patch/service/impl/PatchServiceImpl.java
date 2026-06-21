package com.aicode.patch.service.impl;

import com.aicode.fix.model.FixSuggestion;
import com.aicode.patch.generator.PatchGenerator;
import com.aicode.patch.model.PatchResult;
import com.aicode.patch.service.PatchCacheService;
import com.aicode.patch.service.PatchService;
import com.aicode.patch.validator.PatchValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatchServiceImpl implements PatchService {

    private final PatchGenerator patchGenerator;
    private final PatchValidator patchValidator;
    private final PatchCacheService cacheService;

    @Override
    public PatchResult generatePatch(FixSuggestion suggestion) {
        if (suggestion == null) return null;

        String fixId = suggestion.getRuleId() + ":" + suggestion.getOriginalCode().hashCode();

        log.info("Patch Generate Start: ruleId={}", suggestion.getRuleId());

        // 1. 查缓存
        PatchResult cached = cacheService.get(fixId);
        if (cached != null) {
            log.info("Cache Hit: fixId={}", fixId);
            return cached;
        }
        log.info("Cache Miss: fixId={}", fixId);

        // 2. 生成 Patch
        PatchResult result = patchGenerator.generate(suggestion);

        // 3. 校验
        boolean valid = patchValidator.validate(result);
        result.setValid(valid);

        if (valid) {
            // 4. 写入缓存
            cacheService.put(fixId, result);
            log.info("Patch Generate Success: patchId={}", result.getPatchId());
        } else {
            log.warn("Patch Generate Fail: 校验不通过 patchId={}", result.getPatchId());
        }

        return result;
    }
}
