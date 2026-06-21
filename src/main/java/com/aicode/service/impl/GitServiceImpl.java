package com.aicode.service.impl;

import com.aicode.common.ResultCode;
import com.aicode.dto.GitStatusResponse;
import com.aicode.entity.Project;
import com.aicode.exception.BusinessException;
import com.aicode.mapper.ProjectMapper;
import com.aicode.service.GitService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Git 仓库操作服务实现
 * 基于 JGit
 */
@Slf4j
@Service
public class GitServiceImpl implements GitService {

    private final ProjectMapper projectMapper;
    private final Path storagePath;
    private final int cloneTimeoutSeconds;

    public GitServiceImpl(ProjectMapper projectMapper,
                          @Value("${git.storage.path}") String storagePath,
                          @Value("${git.clone.timeout-seconds:120}") int cloneTimeoutSeconds) {
        this.projectMapper = projectMapper;
        this.storagePath = Path.of(storagePath);
        this.cloneTimeoutSeconds = cloneTimeoutSeconds;
    }

    @Override
    public String cloneRepository(Long projectId) {
        Project project = getProject(projectId);
        String gitUrl = requireGitUrl(project);
        File localDir = getLocalDir(projectId);

        log.info("开始 Clone: projectId={}, url={}, path={}, timeout={}s",
                projectId, gitUrl, localDir, cloneTimeoutSeconds);

        if (localDir.exists()) {
            log.warn("目录已存在, 先删除再 Clone: {}", localDir);
            deleteDirectory(localDir);
        }

        try {
            // 自动检测远程默认分支
            String branch = resolveRemoteBranch(gitUrl, project.getBranchName());
            log.info("使用分支: projectId={}, branch={}", projectId, branch);

            var cloneCmd = Git.cloneRepository()
                    .setURI(gitUrl)
                    .setDirectory(localDir)
                    .setBranch(branch)
                    .setTimeout(cloneTimeoutSeconds);

            log.info("尝试无凭证 Clone（公开仓库）...");

            try (Git git = cloneCmd.call()) {
                log.info("Clone 成功: projectId={}, branch={}", projectId, branch);

                // 更新数据库中的分支名
                if (!branch.equals(project.getBranchName())) {
                    project.setBranchName(branch);
                    projectMapper.updateById(project);
                    log.info("已更新项目分支: projectId={}, branch={}", projectId, branch);
                }

                return "Clone 成功: " + gitUrl;
            }

        } catch (GitAPIException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("connection") || errorMsg.contains("connect"))) {
                log.error("Clone 网络连接失败: projectId={}, url={}", projectId, gitUrl);
                throw new BusinessException("Git Clone 网络连接失败，请检查仓库地址是否正确、网络是否可达。\n" + errorMsg);
            }
            if (errorMsg != null && errorMsg.contains("Authentication")) {
                log.error("Clone 认证失败: projectId={}, url={}", projectId, gitUrl);
                throw new BusinessException("Git Clone 认证失败，请确认仓库是公开的或配置了凭证。\n" + errorMsg);
            }
            log.error("Clone 失败: projectId={}, error={}", projectId, errorMsg);
            throw new BusinessException("Git Clone 失败: " + errorMsg);
        }
    }

    @Override
    public String pullRepository(Long projectId) {
        Project project = getProject(projectId);
        File localDir = getLocalDir(projectId);

        log.info("开始 Pull: projectId={}", projectId);

        if (!localDir.exists()) {
            log.info("本地目录不存在, 先执行 Clone");
            return cloneRepository(projectId);
        }

        try (Git git = Git.open(localDir)) {
            git.pull()
                    .setTimeout(cloneTimeoutSeconds)
                    .call();

            log.info("Pull 成功: projectId={}", projectId);
            return "Pull 成功";

        } catch (Exception e) {
            log.error("Pull 失败: projectId={}, error={}", projectId, e.getMessage());
            throw new BusinessException("Git Pull 失败: " + e.getMessage());
        }
    }

    @Override
    public String fetchRepository(Long projectId) {
        Project project = getProject(projectId);
        File localDir = getLocalDir(projectId);

        log.info("开始 Fetch: projectId={}", projectId);

        if (!localDir.exists()) {
            log.info("本地目录不存在, 先执行 Clone");
            return cloneRepository(projectId);
        }

        try (Git git = Git.open(localDir)) {
            git.fetch()
                    .setTimeout(cloneTimeoutSeconds)
                    .call();

            log.info("Fetch 成功: projectId={}", projectId);
            return "Fetch 成功";

        } catch (Exception e) {
            log.error("Fetch 失败: projectId={}, error={}", projectId, e.getMessage());
            throw new BusinessException("Git Fetch 失败: " + e.getMessage());
        }
    }

    @Override
    public GitStatusResponse status(Long projectId) {
        Project project = getProject(projectId);
        File localDir = getLocalDir(projectId);

        boolean exists = localDir.exists();

        GitStatusResponse.GitStatusResponseBuilder builder = GitStatusResponse.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .gitUrl(project.getGitUrl())
                .localPath(localDir.getAbsolutePath())
                .branch(project.getBranchName())
                .exists(exists);

        if (exists) {
            try (Git git = Git.open(localDir)) {
                Iterable<RevCommit> logs = git.log().setMaxCount(1).call();
                RevCommit latest = logs.iterator().next();
                builder.latestCommit(latest.getName())
                       .commitMessage(latest.getShortMessage());
            } catch (Exception e) {
                log.warn("读取 Git 日志失败: {}", e.getMessage());
            }
        }

        return builder.build();
    }

    // ============ 私有方法 ============

    /**
     * 解析远程默认分支
     * 优先使用项目配置的分支，如果为 null 则自动检测远程 HEAD 指向的分支
     */
    private String resolveRemoteBranch(String gitUrl, String configuredBranch) {
        if (configuredBranch != null && !configuredBranch.isBlank()) {
            return configuredBranch;
        }

        try {
            log.info("自动检测远程默认分支: {}", gitUrl);
            var lsRemote = Git.lsRemoteRepository()
                    .setRemote(gitUrl)
                    .setTimeout(cloneTimeoutSeconds)
                    .setHeads(true);

            Ref head = lsRemote.callAsMap().get("HEAD");
            if (head != null && head.getTarget() != null) {
                String targetRef = head.getTarget().getName();
                String branch = targetRef.replace("refs/heads/", "");
                log.info("检测到远程默认分支: {}", branch);
                return branch;
            }
        } catch (Exception e) {
            log.warn("自动检测远程分支失败, 使用默认值 'master': {}", e.getMessage());
        }

        return "master";
    }

    private File getLocalDir(Long projectId) {
        return storagePath.resolve(String.valueOf(projectId)).toFile();
    }

    private Project getProject(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }
        return project;
    }

    private String requireGitUrl(Project project) {
        String gitUrl = project.getGitUrl();
        if (gitUrl == null || gitUrl.isBlank()) {
            throw new BusinessException("项目未配置 Git 仓库地址");
        }
        return gitUrl;
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteDirectory(f);
                }
            }
        }
        dir.delete();
    }
}
