package com.aicode.analysis.service.impl;

import com.aicode.analysis.model.ControllerMetadata;
import com.aicode.analysis.model.EndpointInfo;
import com.aicode.analysis.service.ControllerAnalysisService;
import com.aicode.parser.model.AnnotationField;
import com.aicode.parser.model.AnnotationModel;
import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.parser.model.ScanContext;
import com.aicode.parser.model.ScannedClass;
import com.aicode.parser.model.ScannedMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller 分析服务实现
 * <p>
 * 从已解析的 ScannedClass 中提取 Spring Web 注解信息，
 * 生成结构化的 ControllerMetadata。
 * <p>
 * 不重新解析源码，完全基于 ProjectCodeModel/ScanContext 中已有的数据。
 */
@Slf4j
@Service
public class ControllerAnalysisServiceImpl implements ControllerAnalysisService {

    @Override
    public List<ControllerMetadata> extractFromScanContext(ScanContext scanContext) {
        if (scanContext == null || scanContext.getControllers() == null) {
            return List.of();
        }
        return extract(scanContext.getControllers());
    }

    @Override
    public List<ControllerMetadata> extractFromProjectCodeModel(ProjectCodeModel model) {
        if (model == null || model.getClasses() == null) {
            return List.of();
        }
        List<ScannedClass> controllers = model.getClasses().stream()
                .filter(c -> "Controller".equals(c.getClassType()))
                .toList();
        return extract(controllers);
    }

    // ============ 私有方法 ============

    private List<ControllerMetadata> extract(List<ScannedClass> controllers) {
        List<ControllerMetadata> result = new ArrayList<>();

        for (ScannedClass clazz : controllers) {
            ControllerMetadata metadata = buildControllerMetadata(clazz);
            result.add(metadata);
            log.info("Controller 元数据提取: {} -> {} 个端点 (basePath={})",
                    clazz.getClassName(), metadata.getEndpoints().size(), metadata.getBasePath());
        }

        return result;
    }

    private ControllerMetadata buildControllerMetadata(ScannedClass clazz) {
        // 提取类级 basePath
        String basePath = extractBasePath(clazz.getAnnotations());

        // 提取方法级端点
        List<EndpointInfo> endpoints = new ArrayList<>();
        for (ScannedMethod method : clazz.getMethods()) {
            EndpointInfo endpoint = extractEndpoint(method, basePath);
            if (endpoint != null) {
                endpoints.add(endpoint);
            }
        }

        return ControllerMetadata.builder()
                .className(clazz.getClassName())
                .qualifiedName(clazz.getQualifiedName())
                .basePath(basePath)
                .endpoints(endpoints)
                .build();
    }

    /**
     * 从类级注解提取 basePath
     * 查找 @RequestMapping 的 value 或 path 属性
     */
    private String extractBasePath(List<AnnotationModel> annotations) {
        for (AnnotationModel ann : annotations) {
            if ("RequestMapping".equals(ann.getName())) {
                String path = getAnnotationField(ann, "value");
                if (path == null || path.isEmpty()) {
                    path = getAnnotationField(ann, "path");
                }
                return (path != null && !path.isEmpty()) ? path : "";
            }
        }
        return "";
    }

    /**
     * 从方法提取端点信息
     * 识别 @GetMapping / @PostMapping / @PutMapping / @DeleteMapping / @RequestMapping
     */
    private EndpointInfo extractEndpoint(ScannedMethod method, String basePath) {
        for (AnnotationModel ann : method.getAnnotations()) {
            String httpMethod = resolveHttpMethod(ann);
            if (httpMethod == null) continue;

            // 提取方法级路径
            String methodPath = getAnnotationField(ann, "value");
            if (methodPath == null || methodPath.isEmpty()) {
                methodPath = getAnnotationField(ann, "path");
            }
            if (methodPath == null) {
                methodPath = "";
            }

            String fullPath = combinePath(basePath, methodPath);

            return EndpointInfo.builder()
                    .httpMethod(httpMethod)
                    .urlPath(fullPath)
                    .methodName(method.getMethodName())
                    .returnType(method.getReturnType())
                    .lineNumber(method.getStartLine())
                    .build();
        }
        return null;
    }

    /**
     * 根据注解名解析 HTTP 方法
     */
    private String resolveHttpMethod(AnnotationModel ann) {
        String name = ann.getName();

        // 快捷注解
        if ("GetMapping".equals(name)) return "GET";
        if ("PostMapping".equals(name)) return "POST";
        if ("PutMapping".equals(name)) return "PUT";
        if ("DeleteMapping".equals(name)) return "DELETE";
        if ("PatchMapping".equals(name)) return "PATCH";

        // @RequestMapping — 检查 method 属性
        if ("RequestMapping".equals(name)) {
            String methodField = getAnnotationField(ann, "method");
            if (methodField == null || methodField.isEmpty()) {
                return "REQUEST"; // 未指定 method，匹配所有
            }
            return extractHttpMethodFromConstant(methodField);
        }

        return null;
    }

    /**
     * 从 RequestMethod.GET / GET 等格式提取 HTTP 方法名
     */
    private String extractHttpMethodFromConstant(String methodField) {
        // RequestMethod.GET → GET
        int lastDot = methodField.lastIndexOf('.');
        if (lastDot >= 0) {
            return methodField.substring(lastDot + 1);
        }
        return methodField.toUpperCase();
    }

    /**
     * 从注解属性中提取指定 key 的值
     */
    private String getAnnotationField(AnnotationModel ann, String key) {
        for (AnnotationField field : ann.getFields()) {
            if (key.equals(field.getKey())) {
                String value = field.getValue();
                // 去掉引号
                if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return value;
            }
        }
        return null;
    }

    /**
     * 拼接 basePath + methodPath，标准化斜杠
     */
    private String combinePath(String basePath, String methodPath) {
        if (basePath == null) basePath = "";
        if (methodPath == null) methodPath = "";

        if (basePath.isEmpty()) return methodPath;
        if (methodPath.isEmpty()) return basePath;

        if (basePath.endsWith("/") && methodPath.startsWith("/")) {
            return basePath + methodPath.substring(1);
        }
        if (!basePath.endsWith("/") && !methodPath.startsWith("/")) {
            return basePath + "/" + methodPath;
        }
        return basePath + methodPath;
    }
}
