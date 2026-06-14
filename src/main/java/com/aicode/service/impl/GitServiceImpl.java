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

    public GitServiceImpl(ProjectMapper projectMapper,
                          @Value("${git.storage.path}") String storagePath) {
        this.projectMapper = projectMapper;
        this.storagePath = Path.of(storagePath);
    }

    @Override
    public String cloneRepository(Long projectId) {
        Project project = getProject(projectId);
        String gitUrl = requireGitUrl(project);
        File localDir = getLocalDir(projectId);

        log.info("开始 Clone: projectId={}, url={}, path={}", projectId, gitUrl, localDir);

        if (localDir.exists()) {
            log.warn("目录已存在, 先删除再 Clone: {}", localDir);
            deleteDirectory(localDir);
        }

        try (Git git = Git.cloneRepository()
                .setURI(gitUrl)
                .setDirectory(localDir)
                .setBranch(project.getBranchName())
                .call()) {

            log.info("Clone 成功: projectId={}, branch={}", projectId, project.getBranchName());
            return "Clone 成功: " + gitUrl;

        } catch (GitAPIException e) {
            log.error("Clone 失败: projectId={}, error={}", projectId, e.getMessage());
            throw new BusinessException("Git Clone 失败: " + e.getMessage());
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
                    .setCredentialsProvider(buildCredentials())
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
                    .setCredentialsProvider(buildCredentials())
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

    /**
     * 获取项目本地目录
     */
    private File getLocalDir(Long projectId) {
        return storagePath.resolve(String.valueOf(projectId)).toFile();
    }

    /**
     * 查询项目
     */
    private Project getProject(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }
        return project;
    }

    /**
     * 校验 Git URL
     */
    private String requireGitUrl(Project project) {
        String gitUrl = project.getGitUrl();
        if (gitUrl == null || gitUrl.isBlank()) {
            throw new BusinessException("项目未配置 Git 仓库地址");
        }
        return gitUrl;
    }

    /**
     * 构建凭证（V1 暂不传凭证，后续扩展）
     */
    private UsernamePasswordCredentialsProvider buildCredentials() {
        return null;
    }

    /**
     * 递归删除目录
     */
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
