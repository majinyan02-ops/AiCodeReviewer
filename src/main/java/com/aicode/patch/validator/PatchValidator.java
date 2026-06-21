package com.aicode.patch.validator;

import com.aicode.patch.model.PatchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PatchValidator {

    private static final int MAX_CODE_LENGTH = 10000;

    public boolean validate(PatchResult patch) {
        if (patch.getOriginalCode() == null || patch.getOriginalCode().isBlank()) {
            log.warn("Patch Validate Fail: originalCode 为空");
            return false;
        }

        if (patch.getFixedCode() == null || patch.getFixedCode().isBlank()) {
            log.warn("Patch Validate Fail: fixedCode 为空");
            return false;
        }

        if (patch.getPatchContent() == null || patch.getPatchContent().isBlank()) {
            log.warn("Patch Validate Fail: patchContent 为空");
            return false;
        }

        if (patch.getOriginalCode().length() > MAX_CODE_LENGTH
                || patch.getFixedCode().length() > MAX_CODE_LENGTH) {
            log.warn("Patch Validate Fail: 代码长度超过限制");
            return false;
        }

        if (!patch.getPatchContent().contains("--- ")
                || !patch.getPatchContent().contains("+++ ")
                || !patch.getPatchContent().contains("@@")) {
            log.warn("Patch Validate Fail: Diff 格式不正确");
            return false;
        }

        log.info("Patch Validate Success: patchId={}", patch.getPatchId());
        return true;
    }
}
