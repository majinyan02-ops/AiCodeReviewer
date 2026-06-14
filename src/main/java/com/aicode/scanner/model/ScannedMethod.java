package com.aicode.scanner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 扫描到的方法信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScannedMethod {

    /** 方法名 */
    private String methodName;

    /** 返回类型 */
    private String returnType;

    /** 方法上的注解列表 */
    private List<String> annotations;

    /** 是否有 @Transactional */
    private boolean hasTransactional;

    /** 方法行数 */
    private int lineCount;

    /** 起始行号 */
    private int startLine;

    /** 结束行号 */
    private int endLine;

    /** 方法内调用的 Mapper 方法 */
    private List<String> mapperCalls;

    /** 是否有 System.out.println */
    private boolean hasSysOut;
}
