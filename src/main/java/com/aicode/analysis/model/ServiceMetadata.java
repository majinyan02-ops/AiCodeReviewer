package com.aicode.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Service 元数据 — 聚合一个 Service 类的分析结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMetadata {

    /** 简单类名 */
    private String className;

    /** 全限定类名 */
    private String qualifiedName;

    /** 类级注解名列表 */
    @Builder.Default
    private List<String> classAnnotations = new ArrayList<>();

    /** 是否有 @Service 注解 */
    private boolean hasServiceAnnotation;

    /** 类级是否有 @Transactional 注解 */
    private boolean hasTransactional;

    /** 方法列表 */
    @Builder.Default
    private List<ServiceMethodInfo> methods = new ArrayList<>();

    /** 总方法数 */
    private int totalMethodCount;

    /** 事务方法数（类级事务时所有方法都算） */
    private int transactionalMethodCount;

    /** 总代码行数 */
    private int totalLineCount;
}
