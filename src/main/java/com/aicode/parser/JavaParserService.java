package com.aicode.parser;

import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.parser.model.ScanContext;
import com.aicode.parser.model.ScannedClass;

/**
 * Java 代码解析服务接口
 * 基于 JavaParser 实现
 */
public interface JavaParserService {

    /**
     * 扫描指定目录下所有 Java 文件
     */
    ScanContext scan(Long projectId, String sourcePath);

    /**
     * 解析整个项目，返回 ProjectCodeModel
     */
    ProjectCodeModel parseProject(String projectPath);

    /**
     * 扫描单个 Java 文件
     */
    ScannedClass scanFile(String filePath);
}
