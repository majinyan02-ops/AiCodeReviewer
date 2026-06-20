package com.aicode.rule.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则检测结果
 * <p>
 * RuleChecker 基于 ProjectCodeModel + CallGraph 执行检测后输出。
 * 每条规则可以产生多个 RuleResult。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleResult {

    /** 规则编号: RULE-001, RULE-002, ... */
    private String ruleId;

    /** 规则名称: 缺少@Transactional, 缺少日志记录, ... */
    private String ruleName;

    /** 规则描述 */
    private String description;

    /** 严重程度: ERROR / WARNING / INFO */
    private String severity;

    /** 是否通过检测（true = 合规，false = 违规） */
    private boolean passed;

    /** 违规所在的类名 */
    private String className;

    /** 违规所在的方法名 */
    private String methodName;

    /** 违规所在的文件路径 */
    private String filePath;

    /** 违规所在的行号 */
    private int lineNumber;

    /** 问题说明 */
    private String message;

    /** 修复建议 */
    private String suggestion;

    /** 方法体内容哈希，用于 AI 分析缓存命中判断 */
    private String contentHash;
}
