package com.aicode.analysis;

import com.aicode.analysis.model.CallEdge;
import com.aicode.analysis.model.CallGraph;
import com.aicode.analysis.service.CallGraphService;
import com.aicode.parser.JavaParserService;
import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.parser.model.ScannedClass;
import com.aicode.rule.RuleEngine;
import com.aicode.rule.model.RuleResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AnalysisPipelineIntegrationTest {

    @Autowired
    private JavaParserService javaParserService;

    @Autowired
    private CallGraphService callGraphService;

    @Autowired
    private RuleEngine ruleEngine;

    private static String sourcePath;
    private static ProjectCodeModel model;

    @BeforeAll
    static void setup() {
        sourcePath = System.getProperty("user.dir");
    }

    private ProjectCodeModel getModel() {
        if (model == null) {
            model = javaParserService.parseProject(sourcePath);
        }
        return model;
    }

    @Test
    void testParserFindsClasses() {
        ProjectCodeModel m = getModel();

        System.out.println("=== Parser Test ===");
        System.out.println("Total classes: " + m.getClasses().size());

        assertThat(m.getClasses()).isNotEmpty();

        Set<String> classTypes = m.getClasses().stream()
                .map(ScannedClass::getClassType)
                .collect(Collectors.toSet());
        System.out.println("Class types found: " + classTypes);

        assertThat(classTypes).contains("Controller", "Service");
    }

    @Test
    void testCallGraphBuildsCorrectly() {
        ProjectCodeModel m = getModel();
        CallGraph callGraph = callGraphService.buildFromProjectCodeModel(m);

        System.out.println("=== CallGraph Test ===");
        System.out.println("Nodes: " + callGraph.getTotalNodes());
        System.out.println("Edges: " + callGraph.getTotalEdges());
        System.out.println("Root nodes: " + callGraph.getRootNodes().size());

        assertThat(callGraph.getTotalNodes()).isGreaterThan(0);
        assertThat(callGraph.getRootNodes()).isNotEmpty();

        long serviceCallEdges = callGraph.getEdges().stream()
                .filter(e -> "SERVICE_CALL".equals(e.getCallType())).count();
        long mapperCallEdges = callGraph.getEdges().stream()
                .filter(e -> "MAPPER_CALL".equals(e.getCallType())).count();

        System.out.println("SERVICE_CALL edges: " + serviceCallEdges);
        System.out.println("MAPPER_CALL edges: " + mapperCallEdges);

        System.out.println("All edges:");
        for (CallEdge edge : callGraph.getEdges()) {
            System.out.println("  [" + edge.getCallType() + "] "
                    + edge.getCallerClassName() + "." + edge.getCallerMethodName()
                    + " -> " + edge.getCalleeClassName() + "." + edge.getCalleeMethodName());
        }
    }

    @Test
    void testCallGraphProjectIdIsNotNull() {
        ProjectCodeModel m = getModel();
        CallGraph callGraph = callGraphService.buildFromProjectCodeModel(m, 123L);

        assertThat(callGraph.getProjectId()).isEqualTo(123L);
    }

    @Test
    void testRuleEngineExecutesAllRules() {
        List<RuleResult> results = ruleEngine.analyze(999L, sourcePath);

        System.out.println("=== RuleEngine Test ===");
        System.out.println("Total issues found: " + results.size());

        for (RuleResult result : results) {
            System.out.println("[" + result.getRuleId() + "] "
                    + result.getClassName() + "." + result.getMethodName()
                    + " - " + result.getMessage());
        }

        assertThat(results).isNotNull();

        Set<String> ruleIds = results.stream()
                .map(RuleResult::getRuleId)
                .collect(Collectors.toSet());
        System.out.println("Rules triggered: " + ruleIds);

        for (RuleResult result : results) {
            assertThat(result.getRuleId()).isNotNull();
            assertThat(result.getClassName()).isNotNull();
            assertThat(result.getMethodName()).isNotNull();
            assertThat(result.getMessage()).isNotNull();
        }
    }

    @Test
    void testNoNonMapperTypeInMapperEdges() {
        ProjectCodeModel m = getModel();
        CallGraph callGraph = callGraphService.buildFromProjectCodeModel(m);

        List<CallEdge> mapperEdges = callGraph.getEdges().stream()
                .filter(e -> "MAPPER_CALL".equals(e.getCallType()))
                .toList();

        System.out.println("=== Mapper Edge Validation ===");
        System.out.println("MAPPER_CALL edges to validate: " + mapperEdges.size());

        Set<String> mapperClassNames = m.getClasses().stream()
                .filter(c -> "Mapper".equals(c.getClassType()))
                .map(ScannedClass::getClassName)
                .collect(Collectors.toSet());

        System.out.println("Known Mapper classes: " + mapperClassNames);

        for (CallEdge edge : mapperEdges) {
            System.out.println("  Validating: " + edge.getCallerClassName()
                    + "." + edge.getCallerMethodName() + " -> "
                    + edge.getCalleeClassName() + "." + edge.getCalleeMethodName());
            assertThat(mapperClassNames).as(
                    "MAPPER_CALL edge target %s should be a Mapper class",
                    edge.getCalleeClassName())
                    .contains(edge.getCalleeClassName());
        }
    }
}
