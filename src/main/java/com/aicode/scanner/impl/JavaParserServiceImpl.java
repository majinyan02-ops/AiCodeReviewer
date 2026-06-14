package com.aicode.scanner.impl;

import com.aicode.scanner.JavaParserService;
import com.aicode.scanner.model.ProjectCodeModel;
import com.aicode.scanner.model.ScanContext;
import com.aicode.scanner.model.ScannedClass;
import com.aicode.scanner.model.ScannedMethod;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * JavaParser 代码扫描服务实现
 * 负责解析 Java 源码，提取类、方法、注解等结构化信息
 */
@Slf4j
@Service
public class JavaParserServiceImpl implements JavaParserService {

    private static final int MAX_METHOD_LINES = 100;

    @Override
    public ScanContext scan(Long projectId, String sourcePath) {
        long start = System.currentTimeMillis();

        List<ScannedClass> controllers = new ArrayList<>();
        List<ScannedClass> services = new ArrayList<>();
        List<ScannedClass> mappers = new ArrayList<>();
        List<ScannedClass> entities = new ArrayList<>();
        List<ScannedClass> others = new ArrayList<>();

        Path root = Path.of(sourcePath);
        if (!Files.exists(root)) {
            log.warn("扫描目录不存在: {}", sourcePath);
            return buildEmpty(projectId, sourcePath, start);
        }

        List<Path> javaFiles = collectJavaFiles(root);
        log.info("扫描到 {} 个 Java 文件", javaFiles.size());

        for (Path file : javaFiles) {
            try {
                ScannedClass sc = parseFile(file);
                if (sc == null) continue;

                switch (sc.getClassType()) {
                    case "Controller" -> controllers.add(sc);
                    case "Service" -> services.add(sc);
                    case "Mapper" -> mappers.add(sc);
                    case "Entity" -> entities.add(sc);
                    default -> others.add(sc);
                }
            } catch (Exception e) {
                log.warn("解析文件失败: {}, reason: {}", file, e.getMessage());
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("扫描完成: projectId={}, files={}, controllers={}, services={}, mappers={}, entities={}, elapsed={}ms",
                projectId, javaFiles.size(), controllers.size(), services.size(),
                mappers.size(), entities.size(), elapsed);

        return ScanContext.builder()
                .projectId(projectId)
                .rootPath(sourcePath)
                .controllers(controllers)
                .services(services)
                .mappers(mappers)
                .entities(entities)
                .others(others)
                .totalFiles(javaFiles.size())
                .elapsedMs(elapsed)
                .build();
    }

    @Override
    public ScannedClass scanFile(String filePath) {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            log.warn("文件不存在: {}", filePath);
            return null;
        }
        return parseFile(path);
    }

    @Override
    public ProjectCodeModel parseProject(String projectPath) {
        long start = System.currentTimeMillis();

        Path root = Path.of(projectPath);
        if (!Files.exists(root)) {
            log.warn("项目目录不存在: {}", projectPath);
            return ProjectCodeModel.builder()
                    .rootPath(projectPath)
                    .classes(List.of())
                    .totalFiles(0)
                    .elapsedMs(System.currentTimeMillis() - start)
                    .build();
        }

        List<Path> javaFiles = collectJavaFiles(root);
        log.info("parseProject: 扫描到 {} 个 Java 文件", javaFiles.size());

        List<ScannedClass> classes = new ArrayList<>();
        for (Path file : javaFiles) {
            try {
                ScannedClass sc = parseFile(file);
                if (sc != null) {
                    classes.add(sc);
                }
            } catch (Exception e) {
                log.warn("解析文件失败: {}, reason: {}", file, e.getMessage());
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("parseProject 完成: path={}, files={}, classes={}, elapsed={}ms",
                projectPath, javaFiles.size(), classes.size(), elapsed);

        return ProjectCodeModel.builder()
                .rootPath(projectPath)
                .classes(classes)
                .totalFiles(javaFiles.size())
                .elapsedMs(elapsed)
                .build();
    }

    /**
     * 收集目录下所有 .java 文件
     */
    private List<Path> collectJavaFiles(Path root) {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        } catch (IOException e) {
            log.error("遍历目录失败: {}", root, e);
            return List.of();
        }
    }

    /**
     * 解析单个 Java 文件
     */
    private ScannedClass parseFile(Path filePath) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(filePath);

            // 获取主类声明
            return cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                    .filter(c -> !c.isInterface())
                    .findFirst()
                    .map(cls -> buildScannedClass(cu, cls, filePath))
                    .orElse(null);
        } catch (IOException e) {
            log.warn("JavaParser 解析失败: {}", filePath);
            return null;
        }
    }

    /**
     * 构建 ScannedClass
     */
    private ScannedClass buildScannedClass(CompilationUnit cu,
                                            ClassOrInterfaceDeclaration cls,
                                            Path filePath) {
        String className = cls.getNameAsString();
        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");
        String qualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

        List<String> classAnnotations = cls.getAnnotations().stream()
                .map(this::annotationName)
                .toList();

        String classType = detectClassType(cls, classAnnotations, filePath);

        List<ScannedMethod> methods = cls.getMethods().stream()
                .map(m -> buildScannedMethod(m, cu))
                .toList();

        return ScannedClass.builder()
                .className(className)
                .qualifiedName(qualifiedName)
                .classType(classType)
                .filePath(filePath.toString())
                .annotations(classAnnotations)
                .methods(methods)
                .build();
    }

    /**
     * 构建 ScannedMethod
     */
    private ScannedMethod buildScannedMethod(MethodDeclaration method, CompilationUnit cu) {
        String methodName = method.getNameAsString();
        String returnType = method.getType().asString();

        List<String> annotations = method.getAnnotations().stream()
                .map(this::annotationName)
                .toList();

        boolean hasTransactional = annotations.contains("Transactional") ||
                annotations.stream().anyMatch(a -> a.endsWith(".Transactional"));

        // 方法行数
        int startLine = method.getBegin().map(p -> p.line).orElse(0);
        int endLine = method.getEnd().map(p -> p.line).orElse(0);
        int lineCount = endLine - startLine + 1;

        // 检测 System.out.println
        boolean hasSysOut = method.toString().contains("System.out.println") ||
                method.toString().contains("System.err.println");

        // 检测 Mapper 调用
        List<String> mapperCalls = new ArrayList<>();
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodCallExpr call, Void arg) {
                String callStr = call.getNameAsString();
                // 识别常见 Mapper 方法调用模式
                if (callStr.endsWith("Mapper") || isMapperMethod(callStr)) {
                    mapperCalls.add(call.getScope()
                            .map(s -> s + "." + callStr)
                            .orElse(callStr));
                }
                super.visit(call, arg);
            }
        }, null);

        return ScannedMethod.builder()
                .methodName(methodName)
                .returnType(returnType)
                .annotations(annotations)
                .hasTransactional(hasTransactional)
                .lineCount(lineCount)
                .startLine(startLine)
                .endLine(endLine)
                .hasSysOut(hasSysOut)
                .mapperCalls(mapperCalls)
                .build();
    }

    /**
     * 识别类类型
     */
    private String detectClassType(ClassOrInterfaceDeclaration cls,
                                    List<String> annotations,
                                    Path filePath) {
        String pathStr = filePath.toString().replace('\\', '/').toLowerCase();

        // 路径匹配
        if (pathStr.contains("/controller/")) return "Controller";
        if (pathStr.contains("/service/impl/") || pathStr.contains("/service/")) return "Service";
        if (pathStr.contains("/mapper/") || pathStr.contains("/repository/")) return "Mapper";
        if (pathStr.contains("/entity/") || pathStr.contains("/domain/") || pathStr.contains("/model/")) return "Entity";

        // 注解匹配
        for (String ann : annotations) {
            if (ann.contains("RestController") || ann.contains("Controller")) return "Controller";
            if (ann.contains("Service")) return "Service";
            if (ann.contains("Mapper") || ann.contains("Repository")) return "Mapper";
            if (ann.contains("Entity") || ann.contains("Table")) return "Entity";
        }

        // 命名匹配
        String name = cls.getNameAsString();
        if (name.endsWith("Controller")) return "Controller";
        if (name.endsWith("ServiceImpl") || name.endsWith("Service")) return "Service";
        if (name.endsWith("Mapper") || name.endsWith("Repository")) return "Mapper";

        return "Other";
    }

    /**
     * 提取注解短名
     */
    private String annotationName(AnnotationExpr ann) {
        String name = ann.getNameAsString();
        // 处理全限定名: @org.springframework.web.bind.annotation.GetMapping → GetMapping
        int lastDot = name.lastIndexOf('.');
        return lastDot >= 0 ? name.substring(lastDot + 1) : name;
    }

    /**
     * 判断是否为 Mapper 方法名
     */
    private boolean isMapperMethod(String methodName) {
        return methodName.startsWith("select") ||
                methodName.startsWith("insert") ||
                methodName.startsWith("update") ||
                methodName.startsWith("delete") ||
                methodName.startsWith("find") ||
                methodName.startsWith("count") ||
                methodName.startsWith("list") ||
                methodName.startsWith("query");
    }

    /**
     * 构建空结果
     */
    private ScanContext buildEmpty(Long projectId, String rootPath, long start) {
        return ScanContext.builder()
                .projectId(projectId)
                .rootPath(rootPath)
                .controllers(List.of())
                .services(List.of())
                .mappers(List.of())
                .entities(List.of())
                .others(List.of())
                .totalFiles(0)
                .elapsedMs(System.currentTimeMillis() - start)
                .build();
    }
}
