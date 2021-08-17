package com.bajins.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class CommandLineRunnerTest implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {

        System.out.println("这是测试CommandLineRunner的示例。");
    }
}
