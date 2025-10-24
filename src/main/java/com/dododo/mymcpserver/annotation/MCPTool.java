package com.dododo.mymcpserver.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom annotation to mark a class as a tool for the MCP server.
 * Classes annotated with @MCPTool will be automatically discovered and registered.
 * It also includes @Component to ensure the annotated class is registered as a Spring bean.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface MCPTool {
}
