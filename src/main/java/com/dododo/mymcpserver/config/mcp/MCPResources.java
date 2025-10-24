package com.dododo.mymcpserver.config.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Configuration
public class MCPResources {
    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> myResources() {
        String path = "/Users/junkangd/junkangDoc/SecureHub";
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
                            String localPath = request.uri();
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
}
