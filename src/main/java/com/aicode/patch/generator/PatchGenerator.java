package com.aicode.patch.generator;

import com.aicode.patch.model.PatchResult;
import com.aicode.fix.model.FixSuggestion;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PatchGenerator {

    public PatchResult generate(FixSuggestion suggestion) {
        String patchId = "PATCH-" + UUID.randomUUID().toString().substring(0, 8);

        String patchContent = buildDiff(
                suggestion.getOriginalCode(),
                suggestion.getFixedCode(),
                suggestion.getRuleName());

        return PatchResult.builder()
                .patchId(patchId)
                .filePath(suggestion.getRuleName())
                .originalCode(suggestion.getOriginalCode())
                .fixedCode(suggestion.getFixedCode())
                .patchContent(patchContent)
                .valid(true)
                .generatedTime(LocalDateTime.now())
                .build();
    }

    private String buildDiff(String originalCode, String fixedCode, String label) {
        StringBuilder diff = new StringBuilder();
        diff.append("--- ").append(label).append("\n");
        diff.append("+++ ").append(label).append("\n");

        String[] originalLines = splitLines(originalCode);
        String[] fixedLines = splitLines(fixedCode);

        diff.append("@@ -1,").append(originalLines.length)
                .append(" +1,").append(fixedLines.length).append(" @@\n");

        for (String line : originalLines) {
            diff.append("- ").append(line).append("\n");
        }
        for (String line : fixedLines) {
            diff.append("+ ").append(line).append("\n");
        }

        return diff.toString();
    }

    private String[] splitLines(String code) {
        if (code == null || code.isEmpty()) return new String[]{""};
        return code.split("\\n");
    }
}
