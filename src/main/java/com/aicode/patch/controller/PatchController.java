package com.aicode.patch.controller;

import com.aicode.common.Result;
import com.aicode.fix.model.FixSuggestion;
import com.aicode.patch.model.PatchResult;
import com.aicode.patch.service.PatchService;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patch")
@RequiredArgsConstructor
public class PatchController {

    private final PatchService patchService;

    @PostMapping("/generate")
    public Result<PatchResult> generate(@RequestBody PatchRequest request) {
        FixSuggestion suggestion = FixSuggestion.builder()
                .ruleId(request.getRuleId())
                .ruleName(request.getRuleName())
                .severity(request.getSeverity())
                .originalCode(request.getOriginalCode())
                .fixedCode(request.getFixedCode())
                .explanation(request.getExplanation())
                .confidence(request.getConfidence())
                .riskLevel(request.getRiskLevel())
                .build();

        PatchResult result = patchService.generatePatch(suggestion);
        return Result.success(result);
    }

    @Data
    public static class PatchRequest {
        private String ruleId;
        private String ruleName;
        private String severity;
        private String originalCode;
        private String fixedCode;
        private String explanation;
        private double confidence;
        private String riskLevel;
    }
}
