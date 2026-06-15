package com.aicode.analysis.service;

import com.aicode.analysis.model.ServiceMetadata;
import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.parser.model.ScanContext;

import java.util.List;

/**
 * Service 分析服务接口
 * 从已解析的代码模型中提取 Service 元数据
 */
public interface ServiceAnalysisService {

    /**
     * 从 ScanContext 提取 Service 元数据
     */
    List<ServiceMetadata> extractFromScanContext(ScanContext context);

    /**
     * 从 ProjectCodeModel 提取 Service 元数据
     */
    List<ServiceMetadata> extractFromProjectCodeModel(ProjectCodeModel model);
}
