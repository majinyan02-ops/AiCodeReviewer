package com.aicode.fix.controller;

import com.aicode.common.Result;
import com.aicode.fix.model.FixSuggestion;
import com.aicode.fix.service.AutoFixService;
import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fix")
@RequiredArgsConstructor
public class AutoFixController {

    private final AutoFixService autoFixService;

    @PostMapping("/generate")
    public Result<FixSuggestion> generate(@RequestBody FixRequest request) {
        RuleResult ruleResult = RuleResult.builder()
                .ruleId(request.getRuleId())
                .ruleName(request.getRuleName())
                .severity(request.getSeverity())
                .className(request.getClassName())
                .methodName(request.getMethodName())
                .filePath(request.getFilePath())
                .lineNumber(request.getLineNumber())
                .message(request.getMessage())
                .contentHash(request.getContentHash())
                .build();

        FixSuggestion suggestion = autoFixService.generateFix(ruleResult);
        return Result.success(suggestion);
    }

    @Data
    public static class FixRequest {
        private String ruleId;
        private String ruleName;
        private String severity;
        private String className;
        private String methodName;
        private String filePath;
        private int lineNumber;
        private String message;
        private String contentHash;
    }
}
