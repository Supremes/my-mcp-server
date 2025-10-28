package com.dododo.mymcpserver.config.mcp;

import com.dododo.mymcpserver.tools.CEMTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MCPTools {
    @Bean
    public ToolCallbackProvider tools(CEMTools cemTools) {
        return MethodToolCallbackProvider.builder().toolObjects(cemTools).build();
    }
}
