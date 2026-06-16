package com.aicode.parser.model;

import com.aicode.analysis.model.MethodCallInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    @Builder.Default
    private List<AnnotationModel> annotations = new ArrayList<>();

    /** 是否有 @Transactional */
    private boolean hasTransactional;

    /** 方法行数 */
    private int lineCount;

    /** 起始行号 */
    private int startLine;

    /** 结束行号 */
    private int endLine;

    /** 方法内调用的 Mapper 方法 */
    @Builder.Default
    private List<String> mapperCalls = new ArrayList<>();

    /** 方法内调用的 Service 方法 */
    @Builder.Default
    private List<MethodCallInfo> serviceCalls = new ArrayList<>();

    /** 是否有 System.out.println */
    private boolean hasSysOut;

    /** 是否有日志记录（log.info/warn/error/debug） */
    private boolean hasLogging;
}
