package com.aicode.fix.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixSuggestion {

    private String ruleId;

    private String ruleName;

    private String severity;

    private String originalCode;

    private String fixedCode;

    private String explanation;

    private double confidence;

    private String riskLevel;

    private LocalDateTime generatedTime;
}
