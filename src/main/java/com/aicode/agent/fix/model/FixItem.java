package com.aicode.agent.fix.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单条修复项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixItem {

    private String ruleId;

    private String className;

    private String methodName;

    private String severity;

    private String issue;

    private String suggestion;

    private String patchContent;

    private boolean patchGenerated;

    private Long generateDuration;
}
