package com.aicode.service.impl;

import com.aicode.common.ResultCode;
import com.aicode.dto.ProjectCreateRequest;
import com.aicode.dto.ProjectUpdateRequest;
import com.aicode.entity.Project;
import com.aicode.exception.BusinessException;
import com.aicode.mapper.ProjectMapper;
import com.aicode.security.utils.SecurityUtils;
import com.aicode.service.ProjectService;
import com.aicode.vo.ProjectVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 项目服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectVO create(ProjectCreateRequest request) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .gitUrl(request.getGitUrl())
                .branchName(request.getBranchName())
                .creatorId(SecurityUtils.getCurrentUserId())
                .status(1)
                .createTime(LocalDateTime.now())
                .build();

        projectMapper.insert(project);
        log.info("项目创建成功: id={}, name={}", project.getId(), project.getName());

        return toVO(project);
    }

    @Override
    @Transactional
    public ProjectVO update(Long id, ProjectUpdateRequest request) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setGitUrl(request.getGitUrl());
        if (request.getBranchName() != null) {
            project.setBranchName(request.getBranchName());
        }

        projectMapper.updateById(project);
        log.info("项目更新成功: id={}", project.getId());

        return toVO(project);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        projectMapper.deleteById(id);
        log.info("项目删除成功: id={}", id);
    }

    @Override
    public ProjectVO getById(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }
        return toVO(project);
    }

    @Override
    public IPage<ProjectVO> page(Integer current, Integer size) {
        //使用的MyBatisPlus的分页插件
        Page<Project> page = new Page<>(current, size);
        Page<Project> result = projectMapper.selectPage(page,
                new LambdaQueryWrapper<Project>()
                        .orderByDesc(Project::getCreateTime)
        );
        //数据库实体转换为视图对象
        return result.convert(this::toVO);
    }

    /**
     * Entity -> VO
     */
    private ProjectVO toVO(Project project) {
        return ProjectVO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .gitUrl(project.getGitUrl())
                .branchName(project.getBranchName())
                .creatorId(project.getCreatorId())
                .status(project.getStatus())
                .createTime(project.getCreateTime())
                .build();
    }
}
