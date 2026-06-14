package com.aicode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建项目请求 DTO
 */
@Data
public class ProjectCreateRequest {

    @NotBlank(message = "项目名称不能为空")
    @Size(min = 1, max = 100, message = "项目名称长度为1-100位")
    private String name;

    @Size(max = 500, message = "项目描述长度不能超过500位")
    private String description;

    @Size(max = 500, message = "Git仓库地址长度不能超过500位")
    private String gitUrl;

    @Size(max = 50, message = "分支名称长度不能超过50位")
    private String branchName;
}
