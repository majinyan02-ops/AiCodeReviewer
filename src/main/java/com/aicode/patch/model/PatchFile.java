package com.aicode.patch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchFile {

    private String filePath;

    private String patchContent;

    private int lineCount;
}
