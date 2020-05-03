package com.bajins.demo.scheduling;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Date;
import java.util.concurrent.Executor;

/**
 * 这是 Scheduling 的第一种配置方式
 * 动态配置、动态修改触发器
 * Scheduling 默认是单线程执行的，同一个时间触发的是串行执行的，配置多线程运行
 */
@Configuration
@EnableAsync
public class SchedulingConfig implements SchedulingConfigurer, AsyncConfigurer {

    private ThreadPoolTaskScheduler scheduler;

    @Autowired
    public TaskScheduler taskScheduler() {
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(8);
        scheduler.setThreadNamePrefix("task-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        //scheduler.schedule();
        scheduler.initialize();
        return scheduler;
    }

    /**
     * @param scheduledTaskRegistrar
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        // 在程序启动后，会逐步启动50个线程，放在线程池中。
        // 同一个任务会占用1个线程，相同的任务执行时，如果上一次时间点还没执行完那么本次时间点会阻塞一直等待上一次任务执行完成
        //scheduledTaskRegistrar.setScheduler(Executors.newScheduledThreadPool(50));

        // 每个时间点任务启动时，都会创建一个单独的线程来处理。也就是说同一个任务也会启动多个线程处理。
        // 然后在具体执行任务的方法上添加注解@Async
        scheduledTaskRegistrar.setScheduler(scheduler);

        // 第一个为任务的具体逻辑实现，第二个为触发器，动态的定时任务则意味着Trigger需要动态获取
        scheduledTaskRegistrar.addTriggerTask(doTask(), getTrigger());

        //scheduledTaskRegistrar.setCronTasksList();
    }

    @Override
    public Executor getAsyncExecutor() {
        return scheduler;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    private Runnable doTask() {
        return () -> {
            // 业务逻辑
            System.out.println("执行了MyDynamicTask,时间为:" + new Date(System.currentTimeMillis()));
        };
    }

    private Trigger getTrigger() {
        return triggerContext -> {
            // 触发器
            CronTrigger trigger = new CronTrigger("0 0/10 * * * ? ");
            return trigger.nextExecutionTime(triggerContext);
        };
    }
}
