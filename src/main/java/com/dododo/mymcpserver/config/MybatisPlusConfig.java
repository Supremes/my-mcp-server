package com.dododo.mymcpserver.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.dododo.mymcpserver.mapper")
public class MybatisPlusConfig {
}
