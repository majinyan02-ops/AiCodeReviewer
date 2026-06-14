package com.aicode.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectVO {

    private Long id;

    private String name;

    private String description;

    private String gitUrl;

    private String branchName;

    private Long creatorId;

    private Integer status;

    private LocalDateTime createTime;
}
