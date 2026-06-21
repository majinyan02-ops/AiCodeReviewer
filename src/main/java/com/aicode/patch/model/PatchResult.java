package com.aicode.patch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchResult {

    private String patchId;

    private String filePath;

    private String originalCode;

    private String fixedCode;

    private String patchContent;

    private boolean valid;

    private LocalDateTime generatedTime;
}
