package com.aicode.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调用图边 — 表示一个方法调用另一个方法
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallEdge {

    /** 调用方 nodeId */
    private String callerId;

    /** 被调用方 nodeId */
    private String calleeId;

    /** 调用方类名 */
    private String callerClassName;

    /** 被调用方类名 */
    private String calleeClassName;

    /** 调用方方法名 */
    private String callerMethodName;

    /** 被调用方方法名 */
    private String calleeMethodName;

    /** 调用类型: SERVICE_CALL / MAPPER_CALL */
    private String callType;

    /** 调用发生的行号（用于规则定位） */
    private int lineNumber;
}
