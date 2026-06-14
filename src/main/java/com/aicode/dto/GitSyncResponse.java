package com.aicode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Git 同步结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitSyncResponse {

    private Long projectId;

    private String projectName;

    private String gitUrl;

    private String localPath;

    private String branch;

    private String message;
}
