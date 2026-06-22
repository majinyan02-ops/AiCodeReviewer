package com.aicode.agent;

import com.aicode.analysis.model.CallGraph;
import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.rule.model.RuleResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一 Agent 输入上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentContext {

    private String projectId;

    private Long reviewId;

    private ProjectCodeModel projectCodeModel;

    private CallGraph callGraph;

    @Builder.Default
    private List<RuleResult> ruleResults = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();
}
