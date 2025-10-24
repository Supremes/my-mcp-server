package com.dododo.mymcpserver.config;

import com.dododo.mymcpserver.tools.CEMTools;
import com.dododo.mymcpserver.tools.DawnTools;
import com.dododo.mymcpserver.tools.SQLTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.RandomAccessFile;
import java.util.stream.Stream;

@Slf4j
@Configuration
public class MCPConfig {
   @Bean
   public ToolCallbackProvider tools(DawnTools dawnTools, SQLTools sqlTools, CEMTools cemTools) {
       return MethodToolCallbackProvider.builder().toolObjects(dawnTools, sqlTools, cemTools).build();
   }

//    @Bean
//    public List<McpServerFeatures.SyncResourceSpecification> myResources() {
//        // 1. 定义资源元数据
//    var systemInfoResource = new Resource(
//        "mcp://system/info",
//        "system-info",
//        "Runtime system environment info in JSON", "",
//        null
//    );
//
//        // 2. 定义同步读取处理器
//        var resourceSpecification = new McpServerFeatures.SyncResourceSpecification(
//                systemInfoResource,
//                (exchange, request) -> {
//                    try {
//                        var runtime = Runtime.getRuntime();
//                        var systemInfo = Map.of(
//                                "os.name", System.getProperty("os.name"),
//                                "os.arch", System.getProperty("os.arch"),
//                                "java.version", System.getProperty("java.version"),
//                                "availableProcessors", runtime.availableProcessors(),
//                                "freeMemoryBytes", runtime.freeMemory(),
//                                "totalMemoryBytes", runtime.totalMemory(),
//                                "timeMillis", System.currentTimeMillis()
//                        );
//
//                        String jsonContent = new ObjectMapper().writeValueAsString(systemInfo);
//
//                        var contents = new TextResourceContents(
//                                systemInfoResource.uri(),     // 资源 URI
//                                "application/json",           // MIME
//                                jsonContent                   // 内容
//                        );
//
//                        return new ReadResourceResult(List.of(contents));
//                    } catch (Exception e) {
//                        throw new RuntimeException("Failed to generate system info", e);
//                    }
//                }
//        );
//
//        // 额外: 日志文件资源 (提供应用日志尾部)
//    var appLogResource = new Resource(
//        "mcp://logs/app",
//        "app-log",
//        "Application log tail (plain text)",
//
//    );
//
//        var logResourceSpecification = new McpServerFeatures.SyncResourceSpecification(
//                appLogResource,
//                (exchange, request) -> {
//                    try {
//                        // 解析日志文件路径: 优先系统属性, 然后环境变量, 最后默认值
//                        String pathStr = System.getProperty("app.log.file");
//                        if (pathStr == null || pathStr.isBlank()) {
//                            pathStr = System.getenv("APP_LOG_FILE");
//                        }
//                        if (pathStr == null || pathStr.isBlank()) {
//                            pathStr = "logs/app.log"; // 默认路径
//                        }
//                        Path logPath = Paths.get(pathStr);
//
//                        String content;
//                        if (!Files.exists(logPath)) {
//                            content = "[log not found] " + logPath.toAbsolutePath();
//                        } else if (Files.isDirectory(logPath)) {
//                            content = "[path is a directory] " + logPath.toAbsolutePath();
//                        } else {
//                            // 读取文件尾部 (最多 50KB)
//                            final int maxBytes = 50 * 1024;
//                            long fileLength = Files.size(logPath);
//                            long start = Math.max(0, fileLength - maxBytes);
//                            try (RandomAccessFile raf = new RandomAccessFile(logPath.toFile(), "r")) {
//                                raf.seek(start);
//                                byte[] bytes = new byte[(int) (fileLength - start)];
//                                raf.readFully(bytes);
//                                content = new String(bytes, StandardCharsets.UTF_8);
//                                if (start > 0) {
//                                    content = "[truncated to last " + maxBytes + " bytes]\n" + content;
//                                }
//                            }
//                        }
//
//                        var contents = new TextResourceContents(
//                                appLogResource.uri(),
//                                "text/plain",
//                                content
//                        );
//                        return new ReadResourceResult(List.of(contents));
//                    } catch (Exception e) {
//                        throw new RuntimeException("Failed to read log file", e);
//                    }
//                }
//        );
//
//        // 3. 返回注册列表 (系统信息 + 日志)
//        return List.of(resourceSpecification, logResourceSpecification);
//    }

    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> myResources() {
//        String path = "/mnt/f/work/documents/spring";
        String path = "F://work//documents//spring";
        Path folderPath = Paths.get(path);
        List<McpServerFeatures.SyncResourceSpecification> resourceSpecifications = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(folderPath)) {
            paths.filter(Files::isRegularFile).forEach(filePath -> {
                File currentFile = new File(String.valueOf(filePath));

                // 1. Create the McpSchema.Resource object
                var fileResource = new McpSchema.Resource(
                        currentFile.getAbsolutePath(), // uri
                        currentFile.getName(),                     // name
                        currentFile.getName(),                     // description
                        getMimeType(filePath),                     // mimeType
                        null                                       // annotations
                );

                // 2. Create a SyncResourceSpecification that links the resource to its content reader
                var resourceSpecification = new McpServerFeatures.SyncResourceSpecification(
                        fileResource,
                        (exchange, request) -> { // The read logic
                            String localPath = request.uri().replace("file://", "");
                            Path currentFilePath = Paths.get(localPath);
                            String mimeType = getMimeType(currentFilePath);

                            try {
                                if (mimeType.startsWith("text/") || mimeType.equals("application/json")) {
                                    // Handle text content
                                    String textContent = Files.readString(currentFilePath);
                                    return new McpSchema.ReadResourceResult(
                                            List.of(new McpSchema.TextResourceContents(request.uri(), mimeType, textContent))
                                    );
                                } else {
                                    // Handle binary content (encode to base64)
                                    byte[] fileBytes = Files.readAllBytes(currentFilePath);
                                    String base64Content = Base64.getEncoder().encodeToString(fileBytes);
                                    return new McpSchema.ReadResourceResult(
                                            List.of(new McpSchema.BlobResourceContents(request.uri(), mimeType, base64Content))
                                    );
                                }
                            } catch (IOException e) {
                                log.error("exception message innner: {}", e.getMessage());
                                return null;
                            }
                        });
                resourceSpecifications.add(resourceSpecification);
            });
        } catch (IOException e) {
            // Handle exception
            log.error("exception message outter: {}", e.getMessage());
            return null;
        }
        log.info("mcp-resource: {}", resourceSpecifications);
        return resourceSpecifications;
    }

    // Helper method to get MIME type
    private static String getMimeType(Path filePath) {
        try {
            return Files.probeContentType(filePath);
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> myPrompts() {
        var prompt = new Prompt("greeting", "A friendly greeting prompt",
                List.of(new PromptArgument("name", "The name to greet", true)));

        var promptSpecification = new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
            String nameArgument = (String) getPromptRequest.arguments().get("name");
            if (nameArgument == null) { nameArgument = "friend"; }
            var userMessage = new PromptMessage(Role.USER, new TextContent("Hello " + nameArgument + "! How can I assist you today?"));
            return new GetPromptResult("A personalized greeting message", List.of(userMessage));
        });

        return List.of(promptSpecification);
    }

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> salesAnalystPrompts() {
        // 1. 创建提示词元数据 (Prompt Metadata)
        var salesPrompt = new McpSchema.Prompt(
                "show_tables", // 提示词唯一标识
                "查看DB中所有表单。", // 描述
                List.of(
                        new McpSchema.PromptArgument("dbName", "需要查询的DB,如dawn", false)
                )
        );

        // 2. 创建同步提示词规范 (SyncPromptSpecification)
        var promptSpecification = new McpServerFeatures.SyncPromptSpecification(
                salesPrompt,
                (exchange, getPromptRequest) -> { // 提示词的实现逻辑
                    // 从请求中获取参数
                    String dbName = (String) getPromptRequest.arguments().get("dbName");
                    if (dbName == null) {
                        dbName = "dawn"; // 默认值
                    }

                    // 构建引导LLM使用工具的指令
                    String promptInstruction = String.format("""
                请分析%s的表单。你需要调用 'run-sql-jpa' 工具来获取 '%s' 的详细表单数据
                """, dbName, dbName);

                    // 构建返回给LLM的消息
                    var userMessage = new McpSchema.PromptMessage(
                            McpSchema.Role.USER,
                            new McpSchema.TextContent(promptInstruction)
                    );

                    // 返回结果
                    return new McpSchema.GetPromptResult(
                            "db表单提示",
                            List.of(userMessage)
                    );
                });

        return List.of(promptSpecification);
    }
}
