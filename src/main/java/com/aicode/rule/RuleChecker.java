package com.aicode.rule;

import com.aicode.analysis.model.CallGraph;
import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.rule.model.RuleResult;

import java.util.List;

/**
 * 规则检测统一接口
 * <p>
 * 所有规则必须实现此接口。
 * <p>
 * 约束：
 * - 输入：ProjectCodeModel + CallGraph
 * - 输出：List<RuleResult>
 * - 禁止直接解析源码
 * - 禁止访问数据库
 */
public interface RuleChecker {

    /**
     * 执行规则检测
     *
     * @param model     项目代码模型（AST 解析结果）
     * @param callGraph 调用图（可为 null，部分规则不需要调用链信息）
     * @return 检测结果列表（每个违规项一个 RuleResult）
     */
    List<RuleResult> check(ProjectCodeModel model, CallGraph callGraph);

    /**
     * 获取规则编号
     */
    String getRuleId();

    /**
     * 获取规则名称
     */
    String getRuleName();

    /**
     * 获取规则描述
     */
    String getDescription();
}
