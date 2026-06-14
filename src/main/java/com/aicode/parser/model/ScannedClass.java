package com.aicode.parser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    @Builder.Default
    private List<AnnotationModel> annotations = new ArrayList<>();

    /** 方法列表 */
    @Builder.Default
    private List<ScannedMethod> methods = new ArrayList<>();
}
