package com.bajins.demo.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * https://blog.csdn.net/tales522/article/details/117509125
 * https://blog.csdn.net/u012160163/article/details/79074649
 * https://blog.csdn.net/qq_16038125/article/details/88877604
 * https://blog.csdn.net/songhao007/article/details/107148263
 * https://www.leftso.com/blog/338.html
 * https://zhuanlan.zhihu.com/p/369381637
 * https://blog.csdn.net/Leon_Jinhai_Sun/article/details/106320095
 */
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
