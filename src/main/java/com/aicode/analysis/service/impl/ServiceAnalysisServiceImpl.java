package com.aicode.analysis.service.impl;

import com.aicode.analysis.model.ServiceMetadata;
import com.aicode.analysis.model.ServiceMethodInfo;
import com.aicode.analysis.service.ServiceAnalysisService;
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
 * Service 分析服务实现
 * <p>
 * 从已解析的 ScannedClass 中提取 Service 相关注解和结构信息，
 * 生成结构化的 ServiceMetadata。
 * <p>
 * 不重新解析源码，完全基于 ProjectCodeModel/ScanContext 中已有的 AST 数据。
 * 不实现规则检测、调用链分析、AI 分析。
 */
@Slf4j
@Service
public class ServiceAnalysisServiceImpl implements ServiceAnalysisService {

    @Override
    public List<ServiceMetadata> extractFromScanContext(ScanContext context) {
        if (context == null || context.getServices() == null) {
            return List.of();
        }
        return extract(context.getServices());
    }

    @Override
    public List<ServiceMetadata> extractFromProjectCodeModel(ProjectCodeModel model) {
        if (model == null || model.getClasses() == null) {
            return List.of();
        }
        List<ScannedClass> services = model.getClasses().stream()
                .filter(c -> "Service".equals(c.getClassType()))
                .toList();
        return extract(services);
    }

    // ============ 私有方法 ============

    private List<ServiceMetadata> extract(List<ScannedClass> services) {
        List<ServiceMetadata> result = new ArrayList<>();

        for (ScannedClass clazz : services) {
            ServiceMetadata metadata = buildServiceMetadata(clazz);
            result.add(metadata);
            log.info("Service 元数据提取: {} -> {} 个方法 (事务: {}/{}, 行数: {})",
                    clazz.getClassName(),
                    metadata.getTotalMethodCount(),
                    metadata.getTransactionalMethodCount(),
                    metadata.getTotalMethodCount(),
                    metadata.getTotalLineCount());
        }

        return result;
    }

    private ServiceMetadata buildServiceMetadata(ScannedClass clazz) {
        // 类级注解名列表
        List<String> classAnnotationNames = clazz.getAnnotations().stream()
                .map(AnnotationModel::getName)
                .toList();

        // 是否有 @Service
        boolean hasServiceAnnotation = clazz.getAnnotations().stream()
                .anyMatch(a -> "Service".equals(a.getName()));

        // 类级是否有 @Transactional
        boolean classHasTransactional = clazz.getAnnotations().stream()
                .anyMatch(a -> "Transactional".equals(a.getName())
                        || (a.getQualifiedName() != null && a.getQualifiedName().endsWith(".Transactional")));

        // 构建方法列表
        List<ServiceMethodInfo> methods = new ArrayList<>();
        int totalLineCount = 0;

        for (ScannedMethod method : clazz.getMethods()) {
            ServiceMethodInfo info = buildServiceMethodInfo(method, classHasTransactional);
            methods.add(info);
            totalLineCount += info.getLineCount();
        }

        // 统计事务方法数：类级事务 → 所有方法；否则统计方法级
        int transactionalMethodCount;
        if (classHasTransactional) {
            transactionalMethodCount = methods.size();
        } else {
            transactionalMethodCount = (int) methods.stream()
                    .filter(ServiceMethodInfo::isHasTransactional)
                    .count();
        }

        return ServiceMetadata.builder()
                .className(clazz.getClassName())
                .qualifiedName(clazz.getQualifiedName())
                .classAnnotations(classAnnotationNames)
                .hasServiceAnnotation(hasServiceAnnotation)
                .hasTransactional(classHasTransactional)
                .methods(methods)
                .totalMethodCount(methods.size())
                .transactionalMethodCount(transactionalMethodCount)
                .totalLineCount(totalLineCount)
                .build();
    }

    private ServiceMethodInfo buildServiceMethodInfo(ScannedMethod method,
                                                      boolean classHasTransactional) {
        // 方法注解名列表
        List<String> annotationNames = method.getAnnotations().stream()
                .map(AnnotationModel::getName)
                .toList();

        // 事务判断：类级事务 OR 方法自身事务
        boolean hasTransactional = classHasTransactional || method.isHasTransactional();

        return ServiceMethodInfo.builder()
                .methodName(method.getMethodName())
                .returnType(method.getReturnType())
                .startLine(method.getStartLine())
                .endLine(method.getEndLine())
                .lineCount(method.getLineCount())
                .hasTransactional(hasTransactional)
                .annotations(annotationNames)
                .mapperCalls(new ArrayList<>(method.getMapperCalls()))
                .build();
    }
}
