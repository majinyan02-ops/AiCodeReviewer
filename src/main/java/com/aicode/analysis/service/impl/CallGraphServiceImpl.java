package com.aicode.analysis.service.impl;

import com.aicode.analysis.model.CallEdge;
import com.aicode.analysis.model.CallGraph;
import com.aicode.analysis.model.CallNode;
import com.aicode.analysis.model.MethodCallInfo;
import com.aicode.analysis.service.CallGraphService;
import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.parser.model.ScanContext;
import com.aicode.parser.model.ScannedClass;
import com.aicode.parser.model.ScannedMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 调用图分析服务实现
 * <p>
 * 基于 ScanContext/ProjectCodeModel 中已有的 AST 数据构建调用关系图。
 * 不重新解析源码，不访问数据库，不实现规则检测。
 * <p>
 * 调用链运行时动态计算，不在 CallGraph 中预存 chains。
 */
@Slf4j
@Service
public class CallGraphServiceImpl implements CallGraphService {

    @Override
    public CallGraph buildFromScanContext(ScanContext context) {
        if (context == null) {
            return emptyGraph(null);
        }

        List<ScannedClass> allClasses = new ArrayList<>();
        if (context.getControllers() != null) allClasses.addAll(context.getControllers());
        if (context.getServices() != null) allClasses.addAll(context.getServices());
        if (context.getMappers() != null) allClasses.addAll(context.getMappers());

        return build(context.getProjectId(), allClasses);
    }

    @Override
    public CallGraph buildFromProjectCodeModel(ProjectCodeModel model) {
        if (model == null || model.getClasses() == null) {
            return emptyGraph(null);
        }

        List<ScannedClass> allClasses = model.getClasses().stream()
                .filter(c -> "Controller".equals(c.getClassType())
                        || "Service".equals(c.getClassType())
                        || "Mapper".equals(c.getClassType()))
                .toList();

        return build(null, allClasses);
    }

    @Override
    public CallGraph buildFromProjectCodeModel(ProjectCodeModel model, Long projectId) {
        if (model == null || model.getClasses() == null) {
            return emptyGraph(projectId);
        }

        List<ScannedClass> allClasses = model.getClasses().stream()
                .filter(c -> "Controller".equals(c.getClassType())
                        || "Service".equals(c.getClassType())
                        || "Mapper".equals(c.getClassType()))
                .toList();

        return build(projectId, allClasses);
    }

    // ============ 私有方法 ============

    private CallGraph build(Long projectId, List<ScannedClass> allClasses) {
        // Step 1: 构建节点索引 + 类名查找索引
        Map<String, CallNode> nodes = new LinkedHashMap<>();
        Map<String, String> classNameIndex = new LinkedHashMap<>(); // 小写简单类名 → qualifiedName
        Map<String, String> classTypeIndex = new LinkedHashMap<>(); // qualifiedName → classType

        for (ScannedClass clazz : allClasses) {
            String simpleNameLower = clazz.getClassName().toLowerCase();
            classNameIndex.put(simpleNameLower, clazz.getQualifiedName());
            classTypeIndex.put(clazz.getQualifiedName(), clazz.getClassType());

            for (ScannedMethod method : clazz.getMethods()) {
                String nodeId = clazz.getQualifiedName() + "." + method.getMethodName();
                CallNode node = CallNode.builder()
                        .nodeId(nodeId)
                        .className(clazz.getClassName())
                        .qualifiedName(clazz.getQualifiedName())
                        .methodName(method.getMethodName())
                        .classType(clazz.getClassType())
                        .filePath(clazz.getFilePath())
                        .lineNumber(method.getStartLine())
                        .build();
                nodes.put(nodeId, node);
            }
        }

        // Step 2: 构建边
        List<CallEdge> edges = new ArrayList<>();
        Set<String> calleeIds = new HashSet<>();

        for (ScannedClass clazz : allClasses) {
            for (ScannedMethod method : clazz.getMethods()) {
                String callerNodeId = clazz.getQualifiedName() + "." + method.getMethodName();

                // Controller/Service 的 serviceCalls → SERVICE_CALL 边
                for (MethodCallInfo call : method.getServiceCalls()) {
                    String resolvedQualifiedName = resolveClassName(
                            call.getTargetClass(), clazz.getQualifiedName(), classNameIndex);
                    if (resolvedQualifiedName == null) {
                        log.debug("无法解析 Service 调用: {}.{}() → {}.{}()",
                                clazz.getClassName(), method.getMethodName(),
                                call.getTargetClass(), call.getTargetMethod());
                        continue;
                    }

                    String calleeNodeId = resolvedQualifiedName + "." + call.getTargetMethod();
                    calleeIds.add(calleeNodeId);

                    edges.add(CallEdge.builder()
                            .callerId(callerNodeId)
                            .calleeId(calleeNodeId)
                            .callerClassName(clazz.getClassName())
                            .calleeClassName(extractSimpleName(resolvedQualifiedName))
                            .callerMethodName(method.getMethodName())
                            .calleeMethodName(call.getTargetMethod())
                            .callType("SERVICE_CALL")
                            .lineNumber(method.getStartLine())
                            .build());
                }

                // Service 的 mapperCalls → MAPPER_CALL 边
                for (String mapperCall : method.getMapperCalls()) {
                    // 解析 "userMapper.insert" → scope=userMapper, method=insert
                    int dotIdx = mapperCall.indexOf('.');
                    String scope = dotIdx > 0 ? mapperCall.substring(0, dotIdx) : "";
                    String mapperMethod = dotIdx > 0 ? mapperCall.substring(dotIdx + 1) : mapperCall;

                    String resolvedQualifiedName = resolveClassName(
                            scope, clazz.getQualifiedName(), classNameIndex);
                    if (resolvedQualifiedName == null) {
                        log.debug("无法解析 Mapper 调用: {}.{}() → {}.{}()",
                                clazz.getClassName(), method.getMethodName(), scope, mapperMethod);
                        continue;
                    }

                    // 验证目标类确实是 Mapper 类型
                    String resolvedType = classTypeIndex.get(resolvedQualifiedName);
                    if (!"Mapper".equals(resolvedType)) {
                        log.debug("非 Mapper 类型，跳过: {}.{}() → {}.{}() (type={})",
                                clazz.getClassName(), method.getMethodName(), scope, mapperMethod, resolvedType);
                        continue;
                    }

                    String calleeNodeId = resolvedQualifiedName + "." + mapperMethod;
                    calleeIds.add(calleeNodeId);

                    edges.add(CallEdge.builder()
                            .callerId(callerNodeId)
                            .calleeId(calleeNodeId)
                            .callerClassName(clazz.getClassName())
                            .calleeClassName(extractSimpleName(resolvedQualifiedName))
                            .callerMethodName(method.getMethodName())
                            .calleeMethodName(mapperMethod)
                            .callType("MAPPER_CALL")
                            .lineNumber(method.getStartLine())
                            .build());
                }
            }
        }

        // Step 3: 识别根节点 — Controller 方法且不是任何边的 callee
        List<String> rootNodes = new ArrayList<>();
        for (ScannedClass clazz : allClasses) {
            if (!"Controller".equals(clazz.getClassType())) continue;
            for (ScannedMethod method : clazz.getMethods()) {
                String nodeId = clazz.getQualifiedName() + "." + method.getMethodName();
                if (!calleeIds.contains(nodeId)) {
                    rootNodes.add(nodeId);
                }
            }
        }

        // Step 4: 组装
        log.info("CallGraph 构建完成: projectId={}, nodes={}, edges={}, rootNodes={}",
                projectId, nodes.size(), edges.size(), rootNodes.size());

        return CallGraph.builder()
                .projectId(projectId)
                .nodes(nodes)
                .edges(edges)
                .rootNodes(rootNodes)
                .totalNodes(nodes.size())
                .totalEdges(edges.size())
                .build();
    }

    /**
     * 根据 scope 名称解析全限定类名
     * <p>
     * 匹配策略：
     * 1. 精确匹配：scope 小写 = classNameIndex key
     * 2. 后缀匹配：key 以 scope 小写结尾（如 userService → userserviceimpl）
     * 3. 前缀匹配：key 以 scope 小写开头
     */
    private String resolveClassName(String scope, String callerQualifiedName,
                                    Map<String, String> classNameIndex) {
        if (scope == null || scope.isEmpty()) return null;
        String scopeLower = scope.toLowerCase();

        // 1. 精确匹配
        if (classNameIndex.containsKey(scopeLower)) {
            return classNameIndex.get(scopeLower);
        }

        // 2. 后缀匹配：如 userService → userserviceimpl
        for (Map.Entry<String, String> entry : classNameIndex.entrySet()) {
            if (entry.getKey().endsWith(scopeLower)) {
                return entry.getValue();
            }
        }

        // 3. 前缀匹配：如 userService → userservice
        for (Map.Entry<String, String> entry : classNameIndex.entrySet()) {
            if (entry.getKey().startsWith(scopeLower)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private String extractSimpleName(String qualifiedName) {
        if (qualifiedName == null) return "";
        int lastDot = qualifiedName.lastIndexOf('.');
        return lastDot >= 0 ? qualifiedName.substring(lastDot + 1) : qualifiedName;
    }

    private CallGraph emptyGraph(Long projectId) {
        return CallGraph.builder()
                .projectId(projectId)
                .nodes(Map.of())
                .edges(List.of())
                .rootNodes(List.of())
                .totalNodes(0)
                .totalEdges(0)
                .build();
    }
}
