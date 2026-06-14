package com.aicode.scanner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 扫描到的类信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScannedClass {

    /** 类名 */
    private String className;

    /** 全限定类名 */
    private String qualifiedName;

    /** 类类型: Controller / Service / Mapper / Entity / Other */
    private String classType;

    /** 文件路径 */
    private String filePath;

    /** 类上的注解列表 */
    private List<String> annotations;

    /** 方法列表 */
    private List<ScannedMethod> methods;
}
