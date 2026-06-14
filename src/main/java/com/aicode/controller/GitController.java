package com.aicode.controller;

import com.aicode.common.Result;
import com.aicode.dto.GitStatusResponse;
import com.aicode.service.GitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Git 仓库管理控制器
 */
@RestController
@RequestMapping("/api/git")
@RequiredArgsConstructor
public class GitController {

    private final GitService gitService;

    /**
     * 同步仓库（Clone / Pull）
     */
    @PostMapping("/sync/{projectId}")
    public Result<String> sync(@PathVariable Long projectId) {
        String result = gitService.pullRepository(projectId);
        return Result.success(result);
    }

    /**
     * 获取仓库状态
     */
    @GetMapping("/status/{projectId}")
    public Result<GitStatusResponse> status(@PathVariable Long projectId) {
        GitStatusResponse status = gitService.status(projectId);
        return Result.success(status);
    }
}
