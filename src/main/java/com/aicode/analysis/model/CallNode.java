package com.aicode.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调用图节点 — 表示一个方法
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallNode {

    /** 唯一标识: qualifiedClassName.methodName */
    private String nodeId;

    /** 简单类名 */
    private String className;

    /** 全限定类名 */
    private String qualifiedName;

    /** 方法名 */
    private String methodName;

    /** 类类型: Controller / Service / Mapper */
    private String classType;

    /** 源文件路径 */
    private String filePath;

    /** 方法起始行号 */
    private int lineNumber;
}
