package com.lu.luaicode;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.lu.luaicode.mapper")
public class LuAiCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuAiCodeApplication.class, args);
    }

}
