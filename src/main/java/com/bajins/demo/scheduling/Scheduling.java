package com.bajins.demo.scheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Spring 3.0 版本之后自带的定时任务
 * 需要在启动类上增加注解@EnableScheduling，启用Scheduled定时任务机制
 */
@Component
public class Scheduling {

    /**
     * 支持cron、fixedDelay、fixedRate三种定义方式，方法必须没有参数，返回void类型
     */
    @Scheduled(cron = "* * * * * ?")
    public void test() {
        System.out.println(Thread.currentThread().getName() + " cron=* * * * * ? --- " + new Date());
    }
}
