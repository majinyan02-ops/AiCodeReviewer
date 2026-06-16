package com.aicode.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 方法调用信息 — 表示一个方法调用表达式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodCallInfo {

    /** 目标类名（scope 名称，如 userService） */
    private String targetClass;

    /** 目标方法名（如 save） */
    private String targetMethod;

    /** 调用类型: SERVICE_CALL / MAPPER_CALL */
    private String callType;
}
