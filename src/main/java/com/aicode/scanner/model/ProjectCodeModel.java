package com.aicode.scanner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目代码模型 — 包含所有扫描到的类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCodeModel {

    /** 项目根路径 */
    private String rootPath;

    /** 扫描到的所有类 */
    @Builder.Default
    private List<ScannedClass> classes = new ArrayList<>();

    /** 扫描文件总数 */
    private int totalFiles;

    /** 扫描耗时（毫秒） */
    private long elapsedMs;
}
