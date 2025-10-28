package com.dododo.mymcpserver.config.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

@Configuration
public class MCPPrompts {
    
    // 默认配置常量
    private static final String DEFAULT_SITE_NAME = "zzwsite1.xmqa.cloud.com";
    private static final String DEFAULT_LOGIN_USER = "junkangd";
    private static final String DEFAULT_PASSWORD = "123456";
    private static final String DEFAULT_USER_NAME = "junkang";
    private static final String DEFAULT_APP_NAME = "junkang";


    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> mcpPrompts() {
        List<McpServerFeatures.SyncPromptSpecification> prompts =  new ArrayList<>();
        prompts.add(createCEMSiteCertsPrompt());
        prompts.add(createCEMDeviceCertsPrompts());
        prompts.add(createCEMDeviceMDXApplicationPrompts());
        return prompts;
    }
    
    /**
     * 创建提示词规范的通用方法
     */
    private McpServerFeatures.SyncPromptSpecification createPromptSpecification(
            String name,
            String description,
            List<McpSchema.PromptArgument> arguments,
            Function<Map<String, Object>, String> instructionBuilder,
            String resultDescription) {
        
        var prompt = new McpSchema.Prompt(name, description, arguments);
        
        return new McpServerFeatures.SyncPromptSpecification(
                prompt,
                (exchange, getPromptRequest) -> {
                    String instruction = instructionBuilder.apply(getPromptRequest.arguments());
                    
                    var userMessage = new McpSchema.PromptMessage(
                            McpSchema.Role.USER,
                            new McpSchema.TextContent(instruction)
                    );
                    
                    return new McpSchema.GetPromptResult(resultDescription, List.of(userMessage));
                });
    }
    
    /**
     * 从参数中获取值，如果不存在则使用默认值
     */
    private String getArgument(Map<String, Object> arguments, String key, String defaultValue) {
        String value = (String) arguments.get(key);
        return value != null ? value : defaultValue;
    }

    public McpServerFeatures.SyncPromptSpecification createCEMSiteCertsPrompt() {
        return createPromptSpecification(
                "site_certs",
                "查询并分析指定 CEM 站点的 SSL/TLS 证书信息，包括证书有效期、颁发者、主题等详细信息。",
                List.of(
                        new McpSchema.PromptArgument(
                                "siteName",
                                "CEM 服务器地址 (例如: zzwsite1.xmqa.cloud.com)",
                                false
                        ),
                        new McpSchema.PromptArgument(
                                "loginUser",
                                "登录用户名 (例如: junkangd)",
                                false
                        ),
                        new McpSchema.PromptArgument(
                                "password",
                                "登录密码",
                                false
                        )
                ),
                arguments -> {
                    String siteName = getArgument(arguments, "siteName", DEFAULT_SITE_NAME);
                    String loginUser = getArgument(arguments, "loginUser", DEFAULT_LOGIN_USER);
                    String password = getArgument(arguments, "password", DEFAULT_PASSWORD);
                    return String.format("""
                            请查询并分析 %s 的 SSL/TLS 证书信息。
                            
                            执行步骤:
                            1. 调用 authenticateUser 工具获取认证令牌 (auth_token)
                               - 服务器地址: %s
                               - 用户名: %s
                               - 密码: %s
                            
                            2. 调用 getSiteCerts 工具获取证书详细信息
                               - 传入参数: auth_token
                            
                            3. 分析并总结证书信息，重点关注:
                               - 证书有效期和过期时间
                               - 证书颁发者和主题
                               - 证书类型和用途
                               - 潜在的安全风险
                            """, siteName, siteName, loginUser, password);
                },
                "CEM 站点证书分析提示"
        );
    }

    @Bean
    public McpServerFeatures.SyncPromptSpecification createCEMDeviceCertsPrompts() {
        return createPromptSpecification(
                "device_certs",
                "查询并分析指定用户设备上安装的证书信息，包括客户端证书、根证书等配置详情。",
                List.of(
                        new McpSchema.PromptArgument(
                                "siteName",
                                "CEM 服务器地址 (例如: zzwsite1.xmqa.cloud.com)",
                                false
                        ),
                        new McpSchema.PromptArgument(
                                "loginUser",
                                "登录用户名 (例如: junkangd)",
                                false
                        ),
                        new McpSchema.PromptArgument(
                                "password",
                                "登录密码",
                                false
                        ),
                        new McpSchema.PromptArgument(
                                "userName",
                                "要查询的目标用户名 (例如: junkang)",
                                false
                        )
                ),
                arguments -> {
                    String siteName = getArgument(arguments, "siteName", DEFAULT_SITE_NAME);
                    String loginUser = getArgument(arguments, "loginUser", DEFAULT_LOGIN_USER);
                    String password = getArgument(arguments, "password", DEFAULT_PASSWORD);
                    String userName = getArgument(arguments, "userName", DEFAULT_USER_NAME);
                    
                    return String.format("""
                            请查询并分析用户 %s 的设备证书信息。
                            
                            执行步骤:
                            1. 调用 authenticateUser 工具进行身份认证
                               - 服务器地址: %s
                               - 用户名: %s
                               - 密码: %s
                               - 获取 auth_token
                            
                            2. 调用 getDevicesByFilter 工具获取用户设备列表
                               - 传入参数: auth_token
                               - 筛选条件: 用户名=%s
                            
                            3. 选择其中一台设备，调用 getDeviceInformationById 工具
                               - 传入参数: deviceId, auth_token
                            
                            4. 分析设备的证书配置，重点关注:
                               - 已安装的客户端证书
                               - 根证书和中间证书
                               - 证书绑定和用途
                               - 证书过期状态
                            """, userName, siteName, loginUser, password, userName);
                },
                "设备证书分析提示"
        );
    }

    public McpServerFeatures.SyncPromptSpecification createCEMDeviceMDXApplicationPrompts() {
        return createPromptSpecification(
                "mdx_applications",
                "查询并分析 iOS 平台上的 MDX 应用配置，包括应用策略、安全设置、网络配置等详细信息。",
                List.of(
                        new McpSchema.PromptArgument(
                                "siteName",
                                "CEM 服务器地址 (例如: zzwsite1.xmqa.cloud.com)",
                                false
                        ),
                        new McpSchema.PromptArgument(
                                "loginUser",
                                "登录用户名 (例如: junkangd)",
                                false
                        ),
                        new McpSchema.PromptArgument(
                                "password",
                                "登录密码",
                                false
                        ),
                        new McpSchema.PromptArgument(
                                "appName",
                                "要查询的 MDX 应用名称 (例如: SecureMail)",
                                false
                        )
                ),
                arguments -> {
                    String siteName = getArgument(arguments, "siteName", DEFAULT_SITE_NAME);
                    String loginUser = getArgument(arguments, "loginUser", DEFAULT_LOGIN_USER);
                    String password = getArgument(arguments, "password", DEFAULT_PASSWORD);
                    String appName = getArgument(arguments, "appName", DEFAULT_APP_NAME);
                    
                    return String.format("""
                            请查询并分析 %s 上的 iOS MDX 应用配置。
                            
                            执行步骤:
                            1. 调用 authenticateUser 工具进行身份认证
                               - 服务器地址: %s
                               - 用户名: %s
                               - 密码: %s
                               - 获取 auth_token
                            
                            2. 调用 getApplicationsByFilter 工具获取 MDX 应用列表
                               - 传入参数: auth_token
                               - 过滤条件: filterIds=application.platform.ios,application.type.mdx
                               - 进一步筛选: 应用名称包含 "%s"
                            
                            3. 选择应用后，调用 getMDXMobileAppByContainerId 工具获取详细配置
                               - 传入参数: containerId (从应用列表中获取), auth_token
                            
                            4. 分析并总结 MDX 应用配置，重点关注:
                               - 应用版本和部署信息
                               - MDX 策略配置 (认证、加密、限制等)
                               - 网络和 VPN 设置
                               - 设备安全要求
                               - 应用间交互规则
                               - 潜在的安全风险或配置问题
                            """, siteName, siteName, loginUser, password, appName);
                },
                "MDX 应用配置分析提示"
        );
    }
}
