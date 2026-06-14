package com.aicode.controller;

import com.aicode.common.Result;
import com.aicode.scanner.JavaParserService;
import com.aicode.scanner.model.ScanContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 代码扫描控制器
 */
@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
public class ScanController {

    private final JavaParserService javaParserService;

    /**
     * 扫描项目源码
     */
    @GetMapping("/{projectId}")
    public Result<ScanContext> scan(@PathVariable Long projectId) {
        // 从 Git 同步路径扫描
        String sourcePath = "./git-repos/" + projectId;
        ScanContext context = javaParserService.scan(projectId, sourcePath);
        return Result.success(context);
    }
}
