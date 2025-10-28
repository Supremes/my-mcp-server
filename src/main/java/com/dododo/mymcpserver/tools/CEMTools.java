package com.dododo.mymcpserver.tools;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CEMTools {
    
    private static final String DEFAULT_SERVER = "junkang.xmdev.cloud.com";
    private static final String DEFAULT_USERNAME = "djk";
    private static final String DEFAULT_PASSWORD = "123456";
    private static final String API_BASE_PATH = "/xenmobile/api/v1";
    private static final int PORT = 4443;
    
    private final HttpClient httpClient;
    
    public CEMTools() {
        this.httpClient = HttpClient.newHttpClient();
    }
    
    /**
     * 构建API的完整URL
     */
    private String buildUrl(String serverAddress, String endpoint) {
        String server = (serverAddress == null || serverAddress.isBlank()) ? DEFAULT_SERVER : serverAddress;
        return String.format("https://%s:%d%s%s", server, PORT, API_BASE_PATH, endpoint);
    }
    
    /**
     * 执行HTTP GET请求
     */
    private String executeGet(String url, String authToken) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET();
            
            if (authToken != null && !authToken.isBlank()) {
                requestBuilder.header("Auth_token", authToken);
            }

            log.info("GET Request URL: " + url);
            
            HttpResponse<String> response = httpClient.send(
                requestBuilder.build(), 
                HttpResponse.BodyHandlers.ofString()
            );
            
            return formatResponse(response);
        } catch (Exception e) {
            return "Request failed: " + e.getMessage();
        }
    }
    
    /**
     * 执行HTTP POST请求
     */
    private String executePost(String url, String jsonBody, String authToken) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
            
            if (authToken != null && !authToken.isBlank()) {
                requestBuilder.header("Auth_token", authToken);
            }

            log.info("POST Request URL: " + url);
            log.info("POST Request Body: " + jsonBody);
            
            HttpResponse<String> response = httpClient.send(
                requestBuilder.build(), 
                HttpResponse.BodyHandlers.ofString()
            );
            
            return formatResponse(response);
        } catch (Exception e) {
            return "Request failed: " + e.getMessage();
        }
    }
    
    /**
     * 格式化HTTP响应
     */
    private String formatResponse(HttpResponse<String> response) {
        return "Status: " + response.statusCode() + ", Response: " + response.body();
    }
    @Tool
    public String authenticateUser(
            @ToolParam(description = "服务器地址", required = false) String serverAddress,
            @ToolParam(description = "用户名", required = false) String username,
            @ToolParam(description = "密码", required = false) String password
    ) {
        // 使用默认值
        String user = (username == null || username.isBlank()) ? DEFAULT_USERNAME : username;
        String pass = (password == null || password.isBlank()) ? DEFAULT_PASSWORD : password;
        
        String jsonBody = String.format("{\"login\":\"%s\",\"password\":\"%s\"}", user, pass);
        String url = buildUrl(serverAddress, "/authentication/login");
        
        return executePost(url, jsonBody, null);
    }

    @Tool
    public String getSiteCerts(
            @ToolParam(description = "服务器地址", required = false) String serverAddress,
            @ToolParam(description = "认证令牌(从登录接口获取)", required = true) String authToken
    ) {
        String url = buildUrl(serverAddress, "/certificates");
        return executeGet(url, authToken);
    }

    @Tool
    public String getDevicesByFilter(
            @ToolParam(description = "服务器地址", required = false) String serverAddress,
            @ToolParam(description = "认证令牌(从登录接口获取)", required = true) String authToken,
            @ToolParam(description = "起始索引,用于分页,默认0", required = false) Integer start,
            @ToolParam(description = "每页记录数限制,必填,最大值100", required = true) Integer limit,
            @ToolParam(description = "排序字段,如: ID, SERIAL, IMEI, LASTAUTHDATE等", required = false) String sortColumn,
            @ToolParam(description = "排序顺序,ASC或DESC", required = false) String sortOrder,
            @ToolParam(description = "搜索字符串,可使用设备IMEI或序列号", required = false) String search,
            @ToolParam(description = "是否启用计数,false可提高性能", required = false) Boolean enableCount,
            @ToolParam(description = "过滤器ID数组,如: device.platform.ios, device.ownership.byod等", required = false) String filterIds
    ) {
        int startIndex = (start == null) ? 0 : start;
        
        // 构建 JSON body
        StringBuilder jsonBuilder = new StringBuilder("{");
        jsonBuilder.append("\"start\":").append(startIndex).append(",");
        jsonBuilder.append("\"limit\":").append(limit);
        
        if (sortColumn != null && !sortColumn.isBlank()) {
            jsonBuilder.append(",\"sortColumn\":\"").append(sortColumn).append("\"");
        }
        if (sortOrder != null && !sortOrder.isBlank()) {
            jsonBuilder.append(",\"sortOrder\":\"").append(sortOrder).append("\"");
        }
        if (search != null && !search.isBlank()) {
            jsonBuilder.append(",\"search\":\"").append(search).append("\"");
        }
        if (enableCount != null) {
            jsonBuilder.append(",\"enableCount\":").append(enableCount);
        }
        if (filterIds != null && !filterIds.isBlank()) {
            jsonBuilder.append(",\"filterIds\":[\"").append(filterIds).append("\"]");
        }
        
        jsonBuilder.append("}");
        
        String url = buildUrl(serverAddress, "/device/filter");
        return executePost(url, jsonBuilder.toString(), authToken);
    }

    @Tool
    public String getDeviceInformationById(
            @ToolParam(description = "服务器地址", required = false) String serverAddress,
            @ToolParam(description = "认证令牌(从登录接口获取)", required = true) String authToken,
            @ToolParam(description = "设备ID", required = true) String deviceId
    ) {
        String url = buildUrl(serverAddress, "/device/" + deviceId);
        return executeGet(url, authToken);
    }

    @Tool
    public String getDevicePoliciesById(
            @ToolParam(description = "服务器地址", required = false) String serverAddress,
            @ToolParam(description = "认证令牌(从登录接口获取)", required = true) String authToken,
            @ToolParam(description = "设备ID", required = true) String deviceId
    ) {
        String url = buildUrl(serverAddress, "/device/" + deviceId + "/policies");
        return executeGet(url, authToken);
    }

    @Tool
    public String getApplicationsByFilter(
            @ToolParam(description = "服务器地址", required = false) String serverAddress,
            @ToolParam(description = "认证令牌(从登录接口获取)", required = true) String authToken,
            @ToolParam(description = "起始索引,用于分页,默认0", required = false) Integer start,
            @ToolParam(description = "每页记录数限制,最大值100", required = false) Integer limit,
            @ToolParam(description = "排序字段,如: id, name, appType, creationDate, lastModificationDate, disabled, vppAccount", required = false) String applicationSortColumn,
            @ToolParam(description = "排序顺序,ASC或DESC", required = false) String sortOrder,
            @ToolParam(description = "搜索字符串,过滤包含该文本的应用名称", required = false) String search,
            @ToolParam(description = "是否启用计数,false可提高性能", required = false) Boolean enableCount,
            @ToolParam(description = "过滤器ID数组,如:'application.type.mdx', 'application.platform.ios', 'application.category#CATEGORY@_fn_@app.cat'等", required = false) String filterIds
    ) {
        int startIndex = (start == null) ? 0 : start;
        int limitValue = (limit == null) ? 100 : limit;
        
        // 构建 JSON body
        StringBuilder jsonBuilder = new StringBuilder("{");
        jsonBuilder.append("\"start\":").append(startIndex).append(",");
        jsonBuilder.append("\"limit\":").append(limitValue);
        
        if (applicationSortColumn != null && !applicationSortColumn.isBlank()) {
            jsonBuilder.append(",\"applicationSortColumn\":\"").append(applicationSortColumn).append("\"");
        }
        if (sortOrder != null && !sortOrder.isBlank()) {
            jsonBuilder.append(",\"sortOrder\":\"").append(sortOrder).append("\"");
        }
        if (search != null && !search.isBlank()) {
            jsonBuilder.append(",\"search\":\"").append(search).append("\"");
        }
        if (enableCount != null) {
            jsonBuilder.append(",\"enableCount\":").append(enableCount);
        }
        if (filterIds != null && !filterIds.isBlank()) {
            String[] ids = filterIds.split(",");
            jsonBuilder.append(",\"filterIds\":\"[");
            for (int i = 0; i < ids.length; i++) {
            jsonBuilder.append("'").append(ids[i].trim()).append("'");
            if (i < ids.length - 1) {
            jsonBuilder.append(",");
            }
            }
            jsonBuilder.append("]\"");
        }
        
        jsonBuilder.append("}");
        
        String url = buildUrl(serverAddress, "/application/filter");
        return executePost(url, jsonBuilder.toString(), authToken);
    }

    @Tool
    public String getMDXMobileAppByContainerId(
            @ToolParam(description = "服务器地址", required = false) String serverAddress,
            @ToolParam(description = "认证令牌(从登录接口获取)", required = true) String authToken,
            @ToolParam(description = "容器ID(Container ID)", required = true) String containerId
    ) {
        String url = buildUrl(serverAddress, "/application/mobile/" + containerId);
        return executeGet(url, authToken);
    }

}