package com.dododo.mymcpserver.config;

import com.dododo.mymcpserver.tools.DawnTools;
import com.dododo.mymcpserver.tools.SQLTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MCPToolConfig {
   @Bean
   public ToolCallbackProvider tools(DawnTools dawnTools, SQLTools sqlTools) {
       return MethodToolCallbackProvider.builder().toolObjects(dawnTools, sqlTools).build();
   }
}
