package com.aicode.common;

import lombok.Getter;

/**
 * 统一返回码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "success"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "资源冲突"),

    // 服务端错误 5xx
    FAILED(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // 业务错误 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_EXISTS(1003, "用户已存在"),
    PROJECT_NOT_FOUND(2001, "项目不存在"),
    TASK_NOT_FOUND(3001, "审查任务不存在"),
    RULE_NOT_FOUND(4001, "规则不存在"),
    REPORT_NOT_FOUND(5001, "报告不存在"),

    // Agent 错误 6xxx
    AGENT_NOT_FOUND(6001, "Agent不存在"),
    AGENT_EXECUTION_FAILED(6002, "Agent执行失败");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
