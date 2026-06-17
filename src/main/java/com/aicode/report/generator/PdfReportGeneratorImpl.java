package com.aicode.report.generator;

import com.aicode.report.model.IssueSummary;
import com.aicode.report.model.ReviewReport;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF 报告生成器实现（基于 OpenPDF）
 */
@Slf4j
@Component
public class PdfReportGeneratorImpl implements PdfReportGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public byte[] generate(ReviewReport report) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // 中文字体
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bf, 20, Font.BOLD);
            Font h2Font = new Font(bf, 14, Font.BOLD);
            Font normalFont = new Font(bf, 10, Font.NORMAL);
            Font boldFont = new Font(bf, 10, Font.BOLD);
            Font smallFont = new Font(bf, 8, Font.NORMAL);

            // 标题
            Paragraph title = new Paragraph("代码审查报告", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph subtitle = new Paragraph(
                    "项目: " + report.getProjectName() + "    扫描时间: " +
                            (report.getScanTime() != null ? report.getScanTime().format(FMT) : "-"),
                    smallFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // 问题统计
            document.add(new Paragraph("问题统计", h2Font));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("规则总数: " + report.getTotalRules()
                    + "  |  通过: " + report.getPassedRules()
                    + "  |  失败: " + report.getFailedRules(), normalFont));
            document.add(new Paragraph("ERROR: " + report.getErrorCount()
                    + "  |  WARNING: " + report.getWarningCount()
                    + "  |  INFO: " + report.getInfoCount(), normalFont));
            document.add(new Paragraph(" "));

            // 问题详情
            document.add(new Paragraph("问题详情", h2Font));
            document.add(new Paragraph(" "));

            List<IssueSummary> issues = report.getIssues();
            if (issues.isEmpty()) {
                document.add(new Paragraph("未发现任何问题，代码质量良好。", normalFont));
            } else {
                List<IssueSummary> sorted = issues.stream()
                        .sorted((a, b) -> severityOrder(b.getSeverity()) - severityOrder(a.getSeverity()))
                        .toList();

                int index = 1;
                for (IssueSummary issue : sorted) {
                    // 每条问题新起一页
                    if (index > 1) document.newPage();

                    String icon = "ERROR".equals(issue.getSeverity()) ? "[ERROR]" :
                            "WARNING".equals(issue.getSeverity()) ? "[WARNING]" : "[INFO]";

                    Paragraph p = new Paragraph(index + ". " + icon + " "
                            + issue.getRuleId() + ": " + issue.getRuleName(), boldFont);
                    p.setSpacingAfter(6);
                    document.add(p);

                    document.add(new Paragraph("严重程度: " + issue.getSeverity(), normalFont));
                    document.add(new Paragraph("所在类: " + issue.getClassName(), normalFont));
                    document.add(new Paragraph("所在方法: " + issue.getMethodName() + "()", normalFont));
                    document.add(new Paragraph("文件: " + issue.getFilePath(), normalFont));
                    document.add(new Paragraph("行号: " + issue.getLineNumber(), normalFont));
                    document.add(new Paragraph(" "));

                    if (issue.getReason() != null && !issue.getReason().isEmpty()) {
                        document.add(new Paragraph("问题原因:", boldFont));
                        document.add(new Paragraph(issue.getReason(), normalFont));
                        document.add(new Paragraph(" "));
                    }
                    if (issue.getImpact() != null && !issue.getImpact().isEmpty()) {
                        document.add(new Paragraph("影响分析:", boldFont));
                        document.add(new Paragraph(issue.getImpact(), normalFont));
                        document.add(new Paragraph(" "));
                    }
                    if (issue.getSuggestion() != null && !issue.getSuggestion().isEmpty()) {
                        document.add(new Paragraph("修复建议:", boldFont));
                        document.add(new Paragraph(issue.getSuggestion(), normalFont));
                        document.add(new Paragraph(" "));
                    }

                    index++;
                }
            }

            // AI 总体建议
            document.newPage();
            document.add(new Paragraph("AI 总体建议", h2Font));
            document.add(new Paragraph(" "));
            if (report.getOverallSummary() != null && !report.getOverallSummary().isEmpty()) {
                document.add(new Paragraph(report.getOverallSummary(), normalFont));
            } else {
                document.add(new Paragraph("无", normalFont));
            }

            // 页脚
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("报告由 AI Code Reviewer 自动生成", smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("PDF 生成失败", e);
            return new byte[0];
        }
    }

    private int severityOrder(String severity) {
        return switch (severity) {
            case "ERROR" -> 3;
            case "WARNING" -> 2;
            case "INFO" -> 1;
            default -> 0;
        };
    }
}
