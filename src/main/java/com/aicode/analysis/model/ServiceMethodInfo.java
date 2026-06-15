package com.aicode.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Service 方法信息 — 表示 Service 类中的一个方法
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMethodInfo {

    /** 方法名 */
    private String methodName;

    /** 返回类型 */
    private String returnType;

    /** 起始行号 */
    private int startLine;

    /** 结束行号 */
    private int endLine;

    /** 方法行数 */
    private int lineCount;

    /** 是否有 @Transactional（类级或方法级） */
    private boolean hasTransactional;

    /** 方法上的注解名列表 */
    @Builder.Default
    private List<String> annotations = new ArrayList<>();

    /** Mapper 方法调用（来自 AST 模型已有数据） */
    @Builder.Default
    private List<String> mapperCalls = new ArrayList<>();
}
