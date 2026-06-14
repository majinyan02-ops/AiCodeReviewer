package com.aicode.scanner;

import com.aicode.scanner.model.ProjectCodeModel;
import com.aicode.scanner.model.ScanContext;
import com.aicode.scanner.model.ScannedClass;

/**
 * Java 代码扫描服务接口
 * 基于 JavaParser 实现
 */
public interface JavaParserService {

    /**
     * 扫描指定目录下所有 Java 文件
     *
     * @param projectId  项目ID
     * @param sourcePath 源码根目录
     * @return 扫描结果上下文
     */
    ScanContext scan(Long projectId, String sourcePath);

    /**
     * 解析整个项目，返回 ProjectCodeModel
     *
     * @param projectPath 项目根目录
     * @return 项目代码模型
     */
    ProjectCodeModel parseProject(String projectPath);

    /**
     * 扫描单个 Java 文件
     *
     * @param filePath Java 文件路径
     * @return 扫描到的类信息，如果不是有效 Java 文件则返回 null
     */
    ScannedClass scanFile(String filePath);
}
