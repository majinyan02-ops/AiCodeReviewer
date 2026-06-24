package com.aicode.patch.source;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 源码读取服务
 * 从源码文件读取方法源码片段
 */
@Slf4j
@Component
public class SourceCodeReader {

    /**
     * 读取方法源码片段
     *
     * @param filePath  文件绝对路径（来自 JavaParser 解析结果）
     * @param methodName 方法名
     * @param lineNumber 行号（大约位置）
     * @return 方法源码片段，如果读取失败返回空字符串
     */
    public String readMethodSource(String filePath, String methodName, int lineNumber) {
        try {
            Path fullPath = Paths.get(filePath);
            if (!Files.exists(fullPath)) {
                log.warn("源码文件不存在: {}", fullPath);
                return "";
            }

            String content = Files.readString(fullPath);
            String[] lines = content.split("\n");

            // 找到方法所在位置，提取方法体
            int startLine = findMethodStart(lines, methodName, lineNumber);
            int endLine = findMethodEnd(lines, startLine);

            if (startLine < 0 || endLine <= startLine) {
                // 找不到方法，返回行号附近的内容
                int contextStart = Math.max(0, lineNumber - 20);
                int contextEnd = Math.min(lines.length, lineNumber + 20);
                return String.join("\n", java.util.Arrays.copyOfRange(lines, contextStart, contextEnd));
            }

            return String.join("\n", java.util.Arrays.copyOfRange(lines, startLine, endLine));

        } catch (IOException e) {
            log.error("读取源码失败: filePath={}", filePath, e);
            return "";
        }
    }

    /**
     * 找到方法的起始行（包含方法签名和注解）
     */
    private int findMethodStart(String[] lines, String methodName, int approximateLine) {
        // 从近似行向上搜索方法签名
        int searchStart = Math.max(0, approximateLine - 30);
        int searchEnd = Math.min(lines.length, approximateLine + 5);

        for (int i = searchStart; i < searchEnd; i++) {
            String line = lines[i].trim();
            // 匹配方法签名
            if (line.contains(methodName + "(") && (line.contains("public ") || line.contains("private ")
                    || line.contains("protected ") || line.contains("void ") || line.contains("static "))) {
                // 向上查找注解
                int annotationStart = i;
                while (annotationStart > 0 && lines[annotationStart - 1].trim().startsWith("@")) {
                    annotationStart--;
                }
                return annotationStart;
            }
        }

        // 退而求其次，返回行号附近
        return Math.max(0, approximateLine - 10);
    }

    /**
     * 找到方法的结束行（匹配大括号）
     */
    private int findMethodEnd(String[] lines, int startLine) {
        if (startLine < 0) return -1;

        int braceCount = 0;
        boolean foundOpenBrace = false;

        for (int i = startLine; i < lines.length; i++) {
            String line = lines[i];
            for (char c : line.toCharArray()) {
                if (c == '{') {
                    braceCount++;
                    foundOpenBrace = true;
                } else if (c == '}') {
                    braceCount--;
                    if (foundOpenBrace && braceCount == 0) {
                        return i + 1; // 返回结束行（包含闭括号）
                    }
                }
            }
        }

        // 没找到匹配的闭括号，返回起始行后50行
        return Math.min(lines.length, startLine + 50);
    }
}
