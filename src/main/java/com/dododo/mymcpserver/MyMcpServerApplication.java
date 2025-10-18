package com.dododo.mymcpserver;

//import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dododo.mymcpserver.mapper")
public class MyMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyMcpServerApplication.class, args);
    }

}
