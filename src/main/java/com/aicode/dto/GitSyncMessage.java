package com.aicode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitSyncMessage {

    private Long projectId;

    private String status;

    private String message;
}
