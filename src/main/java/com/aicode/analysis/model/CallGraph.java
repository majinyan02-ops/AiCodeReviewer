package com.aicode.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用图 — 包含所有节点和边，运行时动态计算调用链
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallGraph {

    /** 项目 ID */
    private Long projectId;

    /** 所有节点，key = nodeId */
    @Builder.Default
    private Map<String, CallNode> nodes = new HashMap<>();

    /** 所有有向边 */
    @Builder.Default
    private List<CallEdge> edges = new ArrayList<>();

    /** 入口节点 nodeId 列表（Controller 方法） */
    @Builder.Default
    private List<String> rootNodes = new ArrayList<>();

    /** 节点总数 */
    private int totalNodes;

    /** 边总数 */
    private int totalEdges;
}
