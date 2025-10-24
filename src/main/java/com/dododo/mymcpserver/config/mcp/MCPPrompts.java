package com.dododo.mymcpserver.config.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MCPPrompts {
    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> cemSiteCertsPrompts() {
        // 1. 创建提示词元数据 (Prompt Metadata)
        var salesPrompt = new McpSchema.Prompt(
                "site_certs", 
                "查看site的cert信息。", 
                List.of(
                        new McpSchema.PromptArgument("siteName", "需要查询的site,如zzwsite1.xmqa.cloud.com", false)
                )
        );

        // 2. 创建同步提示词规范 (SyncPromptSpecification)
        var promptSpecification = new McpServerFeatures.SyncPromptSpecification(
                salesPrompt,
                (exchange, getPromptRequest) -> { // 提示词的实现逻辑
                    // 从请求中获取参数
                    String siteName = (String) getPromptRequest.arguments().get("siteName");
                    if (siteName == null) {
                        siteName = "zzwsite1.xmqa.cloud.com"; // 默认值
                    }

                    // 构建引导LLM使用工具的指令
                    String promptInstruction = String.format("""
                请分析%s的证书列表。你需要遵循以下步骤:
                1. 调用 'authenticateUser' 工具来获取 auth_token。
                2. 调用 'getSiteCerts' 工具，传入 siteName 和 auth_token 参数，获取证书信息。
                """, siteName);

                    // 构建返回给LLM的消息
                    var userMessage = new McpSchema.PromptMessage(
                            McpSchema.Role.USER,
                            new McpSchema.TextContent(promptInstruction)
                    );

                    // 返回结果
                    return new McpSchema.GetPromptResult(
                            "site中的certs提示",
                            List.of(userMessage)
                    );
                });

        return List.of(promptSpecification);
    }

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> cemDeviceCertsPrompts() {
        // 1. 创建提示词元数据 (Prompt Metadata)
        var salesPrompt = new McpSchema.Prompt(
                "device_certs",
                "查看某个用户的其中一台device的cert信息。",
                List.of(
                        new McpSchema.PromptArgument("siteName", "需要查询的site,如zzwsite1.xmqa.cloud.com", false),
                        new McpSchema.PromptArgument("loginUser", "登录用户,如junkangd", false),
                        new McpSchema.PromptArgument("password", "登录密码,如password123", false),
                        new McpSchema.PromptArgument("userName", "需要查询的user,如junkang", false)
                )
        );

        // 2. 创建同步提示词规范 (SyncPromptSpecification)
        var promptSpecification = new McpServerFeatures.SyncPromptSpecification(
                salesPrompt,
                (exchange, getPromptRequest) -> { // 提示词的实现逻辑
                    // 从请求中获取参数
                    String userName = (String) getPromptRequest.arguments().get("userName");
                    if (userName == null) {
                        userName = "junkang"; // 默认值
                    }
                        String siteName = (String) getPromptRequest.arguments().get("siteName");
                        if (siteName == null) {
                                siteName = "zzwsite1.xmqa.cloud.com"; // 默认值
                        }
                        String loginUser = (String) getPromptRequest.arguments().get("loginUser");
                        if (loginUser == null) {
                                loginUser = "junkangd"; // 默认值
                        }
                        String password = (String) getPromptRequest.arguments().get("password");
                        if (password == null) {
                                password = "password123"; // 默认值
                        }

                    // 构建引导LLM使用工具的指令
                    String promptInstruction = String.format("""
                请分析%s中的某一台 device的证书列表。你需要遵循以下步骤:
                1. 调用 'authenticateUser' 工具，传入服务器地址: %s，用户名: %s 和密码: %s 来获取 auth_token。
                2. 调用 'getDeviceInformationById' 工具，传入 deviceId 和 auth_token 参数，获取device信息，并分析其中的 cert 信息。
                """, userName, siteName, userName, password);

                    // 构建返回给LLM的消息
                    var userMessage = new McpSchema.PromptMessage(
                            McpSchema.Role.USER,
                            new McpSchema.TextContent(promptInstruction)
                    );

                    // 返回结果
                    return new McpSchema.GetPromptResult(
                            "device中的certs提示",
                            List.of(userMessage)
                    );
                });

        return List.of(promptSpecification);
    }

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> cemDeviceMDXApplicationPrompts() {
        // 1. 创建提示词元数据 (Prompt Metadata)
        var salesPrompt = new McpSchema.Prompt(
                "device_mdx_applications",
                "查看某个用户的其中一台device的MDX Applications信息。",
                List.of(
                        new McpSchema.PromptArgument("siteName", "需要查询的site,如zzwsite1.xmqa.cloud.com", false),
                        new McpSchema.PromptArgument("loginUser", "登录用户,如junkangd", false),
                        new McpSchema.PromptArgument("password", "登录密码,如password123", false)
                )
        );

        // 2. 创建同步提示词规范 (SyncPromptSpecification)
        var promptSpecification = new McpServerFeatures.SyncPromptSpecification(
                salesPrompt,
                (exchange, getPromptRequest) -> { // 提示词的实现逻辑
                        String siteName = (String) getPromptRequest.arguments().get("siteName");
                        if (siteName == null) {
                                siteName = "zzwsite1.xmqa.cloud.com"; // 默认值
                        }
                        String loginUser = (String) getPromptRequest.arguments().get("loginUser");
                        if (loginUser == null) {
                                loginUser = "junkangd"; // 默认值
                        }
                        String password = (String) getPromptRequest.arguments().get("password");
                        if (password == null) {
                                password = "123456"; // 默认值
                        }

                    // 构建引导LLM使用工具的指令
                    String promptInstruction = String.format("""
                请分析%s中的某一个 iOS且应用类型为application.type.mdx的应用。你需要遵循以下步骤:
                1. 调用 'authenticateUser' 工具，传入服务器地址: %s，用户名: %s 和密码: %s 来获取 auth_token。
                2. 调用 'getApplicationsByFilter' 工具, 传入过滤器ID: application.platform.ios 和 application.type.mdx 以及 auth_token 参数，获取MDX应用信息。
                3. 调用 'getMDXMobileAppByContainerId', 分析获取到的MDX应用信息，并总结其中的信息。
                """, siteName, siteName, loginUser, password);

                    // 构建返回给LLM的消息
                    var userMessage = new McpSchema.PromptMessage(
                            McpSchema.Role.USER,
                            new McpSchema.TextContent(promptInstruction)
                    );

                    // 返回结果
                    return new McpSchema.GetPromptResult(
                            "mdx applications提示",
                            List.of(userMessage)
                    );
                });

        return List.of(promptSpecification);
    }
}
