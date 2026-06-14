package com.aicode.scanner;

import com.aicode.scanner.model.ProjectCodeModel;
import com.aicode.scanner.model.ScannedClass;
import com.aicode.scanner.model.ScannedMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task-08 JavaParser 集成测试
 *
 * 测试用例:
 *   扫描 D:/test-project
 *   期望输出: UserController → save(), update(), delete()
 */
@SpringBootTest
@ActiveProfiles("test")
class JavaParserServiceTest {

    @Autowired
    private JavaParserService javaParserService;

    @Test
    void parseProject_shouldFindUserControllerWithMethods() {
        // 1. 扫描测试项目
        ProjectCodeModel model = javaParserService.parseProject("D:/test-project");

        System.out.println("===================================");
        System.out.println("扫描路径: " + model.getRootPath());
        System.out.println("扫描文件数: " + model.getTotalFiles());
        System.out.println("扫描耗时: " + model.getElapsedMs() + "ms");
        System.out.println("===================================");

        // 2. 输出所有类及方法
        for (ScannedClass clazz : model.getClasses()) {
            System.out.println(clazz.getClassName());
            for (ScannedMethod method : clazz.getMethods()) {
                System.out.println("  " + method.getMethodName() + "()");
            }
        }

        System.out.println("===================================");

        // 3. 断言
        assertThat(model.getClasses()).isNotEmpty();

        // 找到 UserController
        ScannedClass userController = model.getClasses().stream()
                .filter(c -> "UserController".equals(c.getClassName()))
                .findFirst()
                .orElse(null);

        assertThat(userController).isNotNull();
        assertThat(userController.getClassType()).isEqualTo("Controller");

        // 验证方法
        assertThat(userController.getMethods()).hasSize(3);
        assertThat(userController.getMethods()).extracting(ScannedMethod::getMethodName)
                .containsExactlyInAnyOrder("save", "update", "delete");

        System.out.println("✅ Task-08 parseProject() 测试通过！");
    }
}
