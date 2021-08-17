package com.bajins.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ApplicationRunnerTest implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {

        System.out.println("这是测试ApplicationRunner接口");
    }
}
