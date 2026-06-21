package com.aicode.parser.impl;

import com.aicode.analysis.model.MethodCallInfo;
import com.aicode.parser.JavaParserService;
import com.aicode.parser.model.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

/**
 * JavaParser 代码解析服务实现
 */
@Slf4j
@Service
public class JavaParserServiceImpl implements JavaParserService {

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

    // ============ 私有方法 ============

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

    private ScannedClass parseFile(Path filePath) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(filePath);
            return cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                    .filter(c -> !c.isInterface()
                            || c.getNameAsString().endsWith("Mapper")
                            || c.getNameAsString().endsWith("Repository"))
                    .findFirst()
                    .map(cls -> buildScannedClass(cu, cls, filePath))
                    .orElse(null);
        } catch (IOException e) {
            log.warn("JavaParser 解析失败: {}", filePath);
            return null;
        }
    }

    private ScannedClass buildScannedClass(CompilationUnit cu,
                                            ClassOrInterfaceDeclaration cls,
                                            Path filePath) {
        String className = cls.getNameAsString();
        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");
        String qualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

        List<AnnotationModel> classAnnotations = cls.getAnnotations().stream()
                .map(this::buildAnnotationModel)
                .toList();

        String classType = detectClassType(cls, classAnnotations, filePath);

        List<ScannedMethod> methods = cls.getMethods().stream()
                .map(m -> buildScannedMethod(m))
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

    private ScannedMethod buildScannedMethod(MethodDeclaration method) {
        String methodName = method.getNameAsString();
        String returnType = method.getType().asString();

        List<AnnotationModel> annotations = method.getAnnotations().stream()
                .map(this::buildAnnotationModel)
                .toList();

        boolean hasTransactional = annotations.stream()
                .anyMatch(a -> a.getName().equals("Transactional")
                        || a.getQualifiedName().endsWith(".Transactional"));

        int startLine = method.getBegin().map(p -> p.line).orElse(0);
        int endLine = method.getEnd().map(p -> p.line).orElse(0);
        int lineCount = endLine - startLine + 1;

        boolean hasSysOut = method.toString().contains("System.out.println")
                || method.toString().contains("System.err.println");

        boolean hasLogging = method.toString().contains("log.info")
                || method.toString().contains("log.warn")
                || method.toString().contains("log.error")
                || method.toString().contains("log.debug");

        String contentHash = md5(method.toString());

        List<String> mapperCalls = new ArrayList<>();
        List<MethodCallInfo> serviceCalls = new ArrayList<>();

        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodCallExpr call, Void arg) {
                String methodName = call.getNameAsString();
                String scope = call.getScope().map(s -> s.toString()).orElse("");

                // Service 调用检测：scope 名称以 Service 结尾
                boolean isServiceCall = !scope.isEmpty() && scope.toLowerCase().endsWith("service");

                if (isServiceCall) {
                    serviceCalls.add(MethodCallInfo.builder()
                            .targetClass(scope)
                            .targetMethod(methodName)
                            .callType("SERVICE_CALL")
                            .build());
                }

                // Mapper 调用检测（排除已识别为 Service 调用的）
                if (!isServiceCall && (methodName.endsWith("Mapper") || isMapperMethod(methodName))) {
                    mapperCalls.add(call.getScope()
                            .map(s -> s + "." + methodName)
                            .orElse(methodName));
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
                .hasLogging(hasLogging)
                .mapperCalls(mapperCalls)
                .serviceCalls(serviceCalls)
                .contentHash(contentHash)
                .build();
    }

    /**
     * 将 JavaParser AnnotationExpr 转为 AnnotationModel
     */
    private AnnotationModel buildAnnotationModel(AnnotationExpr ann) {
        String qualifiedName = ann.getNameAsString();
        int lastDot = qualifiedName.lastIndexOf('.');
        String name = lastDot >= 0 ? qualifiedName.substring(lastDot + 1) : qualifiedName;

        List<AnnotationField> fields = new ArrayList<>();
        if (ann.isMarkerAnnotationExpr()) {
            // @Override, @Transactional — 无属性
        } else if (ann.isSingleMemberAnnotationExpr()) {
            // @RequestMapping("/api") — value 属性
            fields.add(AnnotationField.builder()
                    .key("value")
                    .value(ann.asSingleMemberAnnotationExpr().getMemberValue().toString())
                    .build());
        } else if (ann.isNormalAnnotationExpr()) {
            // @RequestMapping(method = GET, path = "/api")
            for (MemberValuePair pair : ann.asNormalAnnotationExpr().getPairs()) {
                fields.add(AnnotationField.builder()
                        .key(pair.getNameAsString())
                        .value(pair.getValue().toString())
                        .build());
            }
        }

        return AnnotationModel.builder()
                .name(name)
                .qualifiedName(qualifiedName)
                .fields(fields)
                .build();
    }

    private String detectClassType(ClassOrInterfaceDeclaration cls,
                                    List<AnnotationModel> annotations,
                                    Path filePath) {
        String pathStr = filePath.toString().replace('\\', '/').toLowerCase();

        if (pathStr.contains("/controller/")) return "Controller";
        if (pathStr.contains("/service/impl/") || pathStr.contains("/service/")) return "Service";
        if (pathStr.contains("/mapper/") || pathStr.contains("/repository/")) return "Mapper";
        if (pathStr.contains("/entity/") || pathStr.contains("/domain/") || pathStr.contains("/model/")) return "Entity";

        for (AnnotationModel ann : annotations) {
            String n = ann.getName();
            if (n.contains("RestController") || n.contains("Controller")) return "Controller";
            if (n.contains("Service")) return "Service";
            if (n.contains("Mapper") || n.contains("Repository")) return "Mapper";
            if (n.contains("Entity") || n.contains("Table")) return "Entity";
        }

        String name = cls.getNameAsString();
        if (name.endsWith("Controller")) return "Controller";
        if (name.endsWith("ServiceImpl") || name.endsWith("Service")) return "Service";
        if (name.endsWith("Mapper") || name.endsWith("Repository")) return "Mapper";

        return "Other";
    }

    private boolean isMapperMethod(String methodName) {
        return methodName.startsWith("select")
                || methodName.startsWith("insert")
                || methodName.startsWith("update")
                || methodName.startsWith("delete")
                || methodName.startsWith("find")
                || methodName.startsWith("count")
                || methodName.startsWith("list")
                || methodName.startsWith("query");
    }

    private ScanContext buildEmpty(Long projectId, String rootPath, long start) {
        return ScanContext.builder()
                .projectId(projectId)
                .rootPath(rootPath)
                .totalFiles(0)
                .elapsedMs(System.currentTimeMillis() - start)
                .build();
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
