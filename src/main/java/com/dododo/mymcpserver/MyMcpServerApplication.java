package com.dododo.mymcpserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.dododo.mapper")
@SpringBootApplication
public class MyMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyMcpServerApplication.class, args);
    }

}
