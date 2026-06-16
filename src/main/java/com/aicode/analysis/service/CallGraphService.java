package com.aicode.analysis.service;

import com.aicode.analysis.model.CallGraph;
import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.parser.model.ScanContext;

/**
 * 调用图分析服务接口
 * 基于 ProjectCodeModel/ScanContext 构建 Controller → Service → Mapper 调用关系图
 */
public interface CallGraphService {

    /**
     * 从 ScanContext 构建调用图
     */
    CallGraph buildFromScanContext(ScanContext context);

    /**
     * 从 ProjectCodeModel 构建调用图
     */
    CallGraph buildFromProjectCodeModel(ProjectCodeModel model);
}
