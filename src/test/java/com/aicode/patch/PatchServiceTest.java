package com.aicode.patch;

import com.aicode.fix.model.FixSuggestion;
import com.aicode.patch.generator.PatchGenerator;
import com.aicode.patch.model.PatchResult;
import com.aicode.patch.service.PatchService;
import com.aicode.patch.validator.PatchValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PatchServiceTest {

    @Autowired
    private PatchService patchService;

    @Autowired
    private PatchGenerator patchGenerator;

    @Autowired
    private PatchValidator patchValidator;

    private FixSuggestion createTestSuggestion() {
        return FixSuggestion.builder()
                .ruleId("RULE-004")
                .ruleName("System.out.println")
                .severity("WARNING")
                .originalCode("System.out.println(user);")
                .fixedCode("log.info(\"user={}\", user);")
                .explanation("使用Slf4j日志框架替代System.out")
                .confidence(0.95)
                .riskLevel("LOW")
                .build();
    }

    @Test
    void testGeneratePatch() {
        FixSuggestion suggestion = createTestSuggestion();
        PatchResult result = patchService.generatePatch(suggestion);

        assertThat(result).isNotNull();
        assertThat(result.getPatchId()).startsWith("PATCH-");
        assertThat(result.isValid()).isTrue();
        assertThat(result.getPatchContent()).contains("--- ");
        assertThat(result.getPatchContent()).contains("+++ ");
        assertThat(result.getPatchContent()).contains("@@");
    }

    @Test
    void testGeneratePatchCacheHit() {
        FixSuggestion suggestion = createTestSuggestion();

        PatchResult first = patchService.generatePatch(suggestion);
        PatchResult second = patchService.generatePatch(suggestion);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(first.getPatchId()).isEqualTo(second.getPatchId());
    }

    @Test
    void testPatchGenerator() {
        FixSuggestion suggestion = createTestSuggestion();
        PatchResult result = patchGenerator.generate(suggestion);

        assertThat(result).isNotNull();
        assertThat(result.getPatchId()).startsWith("PATCH-");
        assertThat(result.getOriginalCode()).contains("System.out.println");
        assertThat(result.getFixedCode()).contains("log.info");
    }

    @Test
    void testPatchValidator() {
        FixSuggestion suggestion = createTestSuggestion();
        PatchResult result = patchGenerator.generate(suggestion);

        boolean valid = patchValidator.validate(result);
        assertThat(valid).isTrue();
    }

    @Test
    void testPatchValidatorEmptyOriginal() {
        PatchResult result = PatchResult.builder()
                .patchId("PATCH-test")
                .originalCode("")
                .fixedCode("log.info(\"test\");")
                .patchContent("--- test\n+++ test\n@@\n- \n+ log.info(\"test\");")
                .build();

        boolean valid = patchValidator.validate(result);
        assertThat(valid).isFalse();
    }

    @Test
    void testPatchValidatorEmptyFixed() {
        PatchResult result = PatchResult.builder()
                .patchId("PATCH-test")
                .originalCode("System.out.println(\"test\");")
                .fixedCode("")
                .patchContent("--- test\n+++ test\n@@\n- System.out.println(\"test\");\n+ ")
                .build();

        boolean valid = patchValidator.validate(result);
        assertThat(valid).isFalse();
    }

    @Test
    void testPatchValidatorInvalidFormat() {
        PatchResult result = PatchResult.builder()
                .patchId("PATCH-test")
                .originalCode("test")
                .fixedCode("test2")
                .patchContent("this is not a diff")
                .build();

        boolean valid = patchValidator.validate(result);
        assertThat(valid).isFalse();
    }
}
