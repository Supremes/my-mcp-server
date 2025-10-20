package com.dododo.mymcpserver.tools;

import com.dododo.mymcpserver.annotation.MCPTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.*;

@MCPTool
@Component
public class DawnTools {
    @Tool(name = "analyze-log", description = "分析并总结指定路径的日志文件，可以按日志级别过滤")
    public String analyzeLog(
            @ToolParam(description = "要分析的日志文件的完整路径") String logFilePath,
            @ToolParam(description = "要筛选的日志级别, 例如: ERROR, WARN, INFO") String logLevel
    ) {
        try {
            if (logFilePath == null || logFilePath.isBlank()) {
                return "路径为空";
            }
            File f = new File(logFilePath);
            if (!f.exists() || !f.isFile()) {
                return "文件不存在: " + logFilePath;
            }
            List<String> all = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
            Set<String> levels = Set.of("ERROR", "WARN", "INFO", "DEBUG", "TRACE");
            Map<String, Long> counts = all.stream()
                    .flatMap(l -> levels.stream().filter(l::contains))
                    .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
            Stream<String> stream = all.stream();
            if (logLevel != null && !logLevel.isBlank()) {
                String target = logLevel.trim().toUpperCase();
                stream = stream.filter(l -> l.contains(target));
            }
            List<String> filtered = stream.toList();
            StringBuilder sb = new StringBuilder();
            sb.append("总行数: ").append(all.size()).append('\n');
            sb.append("各级别统计: ").append(counts).append('\n');
            if (logLevel != null && !logLevel.isBlank()) {
                sb.append("筛选级别[").append(logLevel).append("]数量: ").append(filtered.size()).append('\n');
            }
            sb.append("前50行示例:").append('\n');
            filtered.stream().limit(50).forEach(l -> sb.append(l).append('\n'));
            return sb.toString();
        } catch (Exception e) {
            return "读取失败: " + e.getMessage();
        }
    }
}
