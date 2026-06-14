package com.aicode.parser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 扫描上下文 - 一次扫描的完整结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanContext {

    /** 项目ID */
    private Long projectId;

    /** 扫描的根目录 */
    private String rootPath;

    /** 扫描到的 Controller */
    @Builder.Default
    private List<ScannedClass> controllers = new ArrayList<>();

    /** 扫描到的 Service */
    @Builder.Default
    private List<ScannedClass> services = new ArrayList<>();

    /** 扫描到的 Mapper */
    @Builder.Default
    private List<ScannedClass> mappers = new ArrayList<>();

    /** 扫描到的 Entity */
    @Builder.Default
    private List<ScannedClass> entities = new ArrayList<>();

    /** 扫描到的其他类 */
    @Builder.Default
    private List<ScannedClass> others = new ArrayList<>();

    /** 扫描的文件总数 */
    private int totalFiles;

    /** 扫描耗时（毫秒） */
    private long elapsedMs;
}
