package com.aicode.parser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 注解模型 - 结构化表示一个 Java 注解
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationModel {

    /** 注解短名: GetMapping, RestController */
    private String name;

    /** 全限定名: org.springframework.web.bind.annotation.GetMapping */
    private String qualifiedName;

    /** 注解属性 */
    @Builder.Default
    private List<AnnotationField> fields = new ArrayList<>();
}
