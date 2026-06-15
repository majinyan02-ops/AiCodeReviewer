package com.aicode.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller 元数据 — 聚合一个 Controller 类的所有 HTTP 端点信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControllerMetadata {

    /** 简单类名 */
    private String className;

    /** 全限定类名 */
    private String qualifiedName;

    /** 类级 @RequestMapping 路径，例如 /api/users */
    private String basePath;

    /** 所有 HTTP 端点 */
    @Builder.Default
    private List<EndpointInfo> endpoints = new ArrayList<>();
}
