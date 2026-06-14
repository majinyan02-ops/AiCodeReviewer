package com.aicode.service;

import com.aicode.dto.ProjectCreateRequest;
import com.aicode.dto.ProjectUpdateRequest;
import com.aicode.vo.ProjectVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 项目服务接口
 */
public interface ProjectService {

    /**
     * 创建项目
     */
    ProjectVO create(ProjectCreateRequest request);

    /**
     * 更新项目
     */
    ProjectVO update(Long id, ProjectUpdateRequest request);

    /**
     * 删除项目
     */
    void delete(Long id);

    /**
     * 获取项目详情
     */
    ProjectVO getById(Long id);

    /**
     * 分页查询项目列表
     */
    IPage<ProjectVO> page(Integer current, Integer size);
}
