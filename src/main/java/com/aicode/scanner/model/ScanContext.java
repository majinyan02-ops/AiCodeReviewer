package com.aicode.scanner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private List<ScannedClass> controllers;

    /** 扫描到的 Service */
    private List<ScannedClass> services;

    /** 扫描到的 Mapper */
    private List<ScannedClass> mappers;

    /** 扫描到的 Entity */
    private List<ScannedClass> entities;

    /** 扫描到的其他类 */
    private List<ScannedClass> others;

    /** 扫描的文件总数 */
    private int totalFiles;

    /** 扫描耗时（毫秒） */
    private long elapsedMs;
}
