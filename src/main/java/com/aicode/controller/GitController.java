package com.aicode.controller;

import com.aicode.common.Result;
import com.aicode.dto.GitStatusResponse;
import com.aicode.service.GitNotifyService;
import com.aicode.service.GitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/git")
@RequiredArgsConstructor
public class GitController {

    private final GitService gitService;
    private final GitNotifyService gitNotifyService;

    /**
     * 同步仓库（Clone / Pull）- 异步执行，通过 WebSocket 通知结果
     */
    @PostMapping("/sync/{projectId}")
    public Result<String> sync(@PathVariable Long projectId) {
        gitNotifyService.notifySyncStart(projectId);

        CompletableFuture.runAsync(() -> {
            try {
                String result = gitService.pullRepository(projectId);
                gitNotifyService.notifySyncSuccess(projectId, result);
            } catch (Exception e) {
                log.error("异步同步失败: projectId={}", projectId, e);
                gitNotifyService.notifySyncError(projectId, e.getMessage());
            }
        });

        return Result.success("同步任务已提交，正在后台执行");
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
