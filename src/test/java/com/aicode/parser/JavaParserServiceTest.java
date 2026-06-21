package com.aicode.parser;

import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.parser.model.ScannedClass;
import com.aicode.parser.model.ScannedMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JavaParserServiceTest {

    @Autowired
    private JavaParserService javaParserService;

    @Test
    void parseProject_shouldFindClasses() {
        String sourcePath = System.getProperty("user.dir");
        ProjectCodeModel model = javaParserService.parseProject(sourcePath);

        System.out.println("===================================");
        System.out.println("扫描路径: " + model.getRootPath());
        System.out.println("扫描文件数: " + model.getTotalFiles());
        System.out.println("扫描类数: " + model.getClasses().size());
        System.out.println("扫描耗时: " + model.getElapsedMs() + "ms");
        System.out.println("===================================");

        for (ScannedClass clazz : model.getClasses()) {
            System.out.println("[" + clazz.getClassType() + "] " + clazz.getClassName()
                    + " (" + clazz.getMethods().size() + " methods)");
        }
        System.out.println("===================================");

        assertThat(model.getClasses()).isNotEmpty();

        long controllers = model.getClasses().stream()
                .filter(c -> "Controller".equals(c.getClassType())).count();
        long services = model.getClasses().stream()
                .filter(c -> "Service".equals(c.getClassType())).count();
        long mappers = model.getClasses().stream()
                .filter(c -> "Mapper".equals(c.getClassType())).count();

        System.out.println("Controllers: " + controllers);
        System.out.println("Services: " + services);
        System.out.println("Mappers: " + mappers);

        assertThat(controllers).isGreaterThan(0);
        assertThat(services).isGreaterThan(0);
    }
}
