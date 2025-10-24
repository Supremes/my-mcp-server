package com.dododo.mymcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

@Component
public class CEMTools {
    @Tool
    public String authenticateUser(
            @ToolParam(description = "服务器地址", required = false) String serverAddress,
            @ToolParam(description = "用户名", required = false) String username,
            @ToolParam(description = "密码", required = false) String password
    ) {
        if (serverAddress == null || serverAddress.isBlank() ||
            username == null || username.isBlank() ||
            password == null || password.isBlank()) {
            username = "djk";
            password = "123456";
            serverAddress = "junkang.xmdev.cloud.com";
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("{\"login\":\"%s\",\"password\":\"%s\"}", username, password);
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://" + serverAddress + ":4443/xenmobile/api/v1/authentication/login"))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
            
            java.net.http.HttpResponse<String> response = client.send(request, 
                java.net.http.HttpResponse.BodyHandlers.ofString());
            
            return "Authentication status: " + response.statusCode() + ", Response: " + response.body();
        } catch (Exception e) {
            return "Authentication failed: " + e.getMessage();
        }
    }

    @Tool
    public String getAllCertificates(
            @ToolParam(description = "服务器地址", required = false) String serverAddress,
            @ToolParam(description = "认证令牌（从登录接口获取）", required = true) String authToken
    ) {
        if (serverAddress == null || serverAddress.isBlank()) {
            serverAddress = "junkang.xmdev.cloud.com";
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://" + serverAddress + ":4443/xenmobile/api/v1/certificates"))
                .header("Content-Type", "application/json")
                .header("Auth_token", authToken)
                .GET()
                .build();
            
            java.net.http.HttpResponse<String> response = client.send(request, 
                java.net.http.HttpResponse.BodyHandlers.ofString());
            
            return "Get certificates status: " + response.statusCode() + ", Response: " + response.body();
        } catch (Exception e) {
            return "Get certificates failed: " + e.getMessage();
        }
    }
}
