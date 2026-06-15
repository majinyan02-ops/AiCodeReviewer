package com.aicode.analysis.service;

import com.aicode.analysis.model.ControllerMetadata;
import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.parser.model.ScanContext;

import java.util.List;

/**
 * Controller 分析服务接口
 * 从已解析的代码模型中提取 HTTP 端点元数据
 */
public interface ControllerAnalysisService {

    /**
     * 从 ScanContext 提取 Controller 元数据
     */
    List<ControllerMetadata> extractFromScanContext(ScanContext scanContext);

    /**
     * 从 ProjectCodeModel 提取 Controller 元数据
     */
    List<ControllerMetadata> extractFromProjectCodeModel(ProjectCodeModel model);
}
