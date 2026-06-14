package com.aicode.service;

import com.aicode.dto.GitStatusResponse;

/**
 * Git 仓库操作服务接口
 * 基于 JGit 实现
 */
public interface GitService {

    /**
     * Clone 仓库到本地
     */
    String cloneRepository(Long projectId);

    /**
     * Pull 拉取最新代码
     */
    String pullRepository(Long projectId);

    /**
     * Fetch 远程更新（不合并）
     */
    String fetchRepository(Long projectId);

    /**
     * 获取仓库状态
     */
    GitStatusResponse status(Long projectId);
}
