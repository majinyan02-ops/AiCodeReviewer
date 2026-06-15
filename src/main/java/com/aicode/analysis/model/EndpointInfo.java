package com.aicode.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HTTP 端点信息 — 表示 Controller 中的一个 HTTP 接口
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointInfo {

    /** HTTP 方法: GET / POST / PUT / DELETE */
    private String httpMethod;

    /** 完整 URL 路径（class basePath + method path） */
    private String urlPath;

    /** Java 方法名 */
    private String methodName;

    /** 返回类型 */
    private String returnType;

    /** 方法起始行号 */
    private int lineNumber;
}
