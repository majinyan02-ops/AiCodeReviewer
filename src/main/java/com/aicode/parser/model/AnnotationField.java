package com.aicode.parser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注解属性键值对
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationField {

    /** 属性名: value, method, name */
    private String key;

    /** 属性值 */
    private String value;
}
