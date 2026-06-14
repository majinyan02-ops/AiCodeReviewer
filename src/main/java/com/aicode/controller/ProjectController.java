package com.aicode.controller;

import com.aicode.common.Result;
import com.aicode.dto.ProjectCreateRequest;
import com.aicode.dto.ProjectUpdateRequest;
import com.aicode.service.ProjectService;
import com.aicode.vo.ProjectVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目管理控制器
 */
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 创建项目
     */
    @PostMapping
    public Result<ProjectVO> create(@Valid @RequestBody ProjectCreateRequest request) {
        ProjectVO vo = projectService.create(request);
        return Result.success(vo);
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public Result<ProjectVO> update(@PathVariable Long id,
                                     @Valid @RequestBody ProjectUpdateRequest request) {
        ProjectVO vo = projectService.update(id, request);
        return Result.success(vo);
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success();
    }

    /**
     * 项目详情
     */
    @GetMapping("/{id}")
    public Result<ProjectVO> getById(@PathVariable Long id) {
        ProjectVO vo = projectService.getById(id);
        return Result.success(vo);
    }

    /**
     * 项目分页列表
     */
    @GetMapping("/page")
    public Result<IPage<ProjectVO>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<ProjectVO> page = projectService.page(current, size);
        return Result.success(page);
    }
}
