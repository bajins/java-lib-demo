package com.bajins.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

// 开启缓存
@EnableCaching
// 使用Java注解快捷配置SpringWebmvc
// @EnableWebMvc=继承DelegatingWebMvcConfiguration=继承WebMvcConfigurationSupport
@EnableWebMvc
// 开启定时任务
@EnableScheduling
@SpringBootApplication
public class BajinsApiApplication {

    // RestTemplate是一个HTTP客户端，使用它我们可以方便的调用HTTP接口
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(BajinsApiApplication.class, args);
    }

}
