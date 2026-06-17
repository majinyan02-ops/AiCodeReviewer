package com.aicode.report.generator;

import com.aicode.report.model.ReviewReport;

import java.io.ByteArrayOutputStream;

/**
 * PDF 报告生成器
 * <p>
 * 将 ReviewReport 渲染为 PDF 文件。
 */
public interface PdfReportGenerator {

    /**
     * 生成 PDF 报告
     *
     * @param report 审查报告
     * @return PDF 字节数组
     */
    byte[] generate(ReviewReport report);
}
