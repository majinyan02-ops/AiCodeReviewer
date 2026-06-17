package com.aicode.ai.service;

import com.aicode.rule.model.RuleResult;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Prompt 服务 — 统一加载和管理 Prompt 模板
 * <p>
 * 所有 Prompt 存放在 resources/prompts/ 目录下，
 * 禁止在业务代码中硬编码 Prompt。
 */
@Slf4j
@Component
public class PromptService {

    private String systemPrompt;
    private String userPromptTemplate;

    @PostConstruct
    public void init() {
        this.systemPrompt = loadFile("prompts/system-prompt.txt");
        this.userPromptTemplate = loadFile("prompts/user-prompt-template.txt");
        log.info("PromptService 初始化完成: systemPrompt={} chars, userTemplate={} chars",
                systemPrompt.length(), userPromptTemplate.length());
    }

    /**
     * 获取 System Prompt
     */
    public String getSystemPrompt() {
        return systemPrompt;
    }

    /**
     * 构建单条 User Prompt
     */
    public String buildUserPrompt(RuleResult r) {
        String issue = formatIssue(r);
        return userPromptTemplate.replace("{issue}", issue);
    }

    /**
     * 构建批量 User Prompt
     */
    public String buildBatchUserPrompt(List<RuleResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("以下是对项目进行静态代码分析后发现的违规问题，请逐一进行深度分析：\n\n");

        for (int i = 0; i < results.size(); i++) {
            sb.append("--- 问题 ").append(i + 1).append(" ---\n");
            sb.append(formatIssue(results.get(i)));
            sb.append("\n");
        }

        sb.append("请对以上 ").append(results.size()).append(" 个问题进行深度分析，并返回以下 JSON 格式：\n");
        sb.append("{\n");
        sb.append("  \"overallScore\": 7,\n");
        sb.append("  \"summary\": \"总体评价...\",\n");
        sb.append("  \"issues\": [\n");
        sb.append("    {\n");
        sb.append("      \"ruleId\": \"RULE-001\",\n");
        sb.append("      \"ruleName\": \"缺少@Transactional\",\n");
        sb.append("      \"className\": \"UserServiceImpl\",\n");
        sb.append("      \"methodName\": \"save\",\n");
        sb.append("      \"riskLevel\": \"高\",\n");
        sb.append("      \"reason\": \"...\",\n");
        sb.append("      \"impact\": \"...\",\n");
        sb.append("      \"suggestion\": \"...\",\n");
        sb.append("      \"exampleFix\": \"...\"\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String formatIssue(RuleResult r) {
        return String.format("""
                        规则编号: %s
                        规则名称: %s
                        严重程度: %s
                        所在类: %s
                        所在方法: %s
                        所在文件: %s
                        行号: %d
                        问题描述: %s
                        """,
                r.getRuleId(), r.getRuleName(), r.getSeverity(),
                r.getClassName(), r.getMethodName(), r.getFilePath(),
                r.getLineNumber(), r.getMessage());
    }

    private String loadFile(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("加载 Prompt 文件失败: {}", path, e);
            return "";
        }
    }
}
