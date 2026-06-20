package com.aicode.cache.controller;

import com.aicode.ai.service.AnalysisCacheService;
import com.aicode.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 缓存管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final AnalysisCacheService analysisCacheService;

    /**
     * 清除指定项目的 AI 分析缓存
     */
    @DeleteMapping("/analysis/{projectId}")
    public Result<Void> clearAnalysisCache(@PathVariable Long projectId) {
        analysisCacheService.clear(projectId);
        return Result.success("已清除项目 " + projectId + " 的 AI 分析缓存", null);
    }

    /**
     * 查询指定项目的缓存统计
     */
    @GetMapping("/stats/{projectId}")
    public Result<Map<String, Object>> stats(@PathVariable Long projectId) {
        long count = analysisCacheService.count(projectId);
        return Result.success(Map.of("projectId", projectId, "cacheCount", count));
    }
}
