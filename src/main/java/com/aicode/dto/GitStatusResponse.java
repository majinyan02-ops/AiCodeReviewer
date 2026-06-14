package com.aicode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Git 仓库状态 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitStatusResponse {

    private Long projectId;

    private String projectName;

    private String gitUrl;

    private String localPath;

    private String branch;

    private String latestCommit;

    private String commitMessage;

    private Boolean exists;
}
