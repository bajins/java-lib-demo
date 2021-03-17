package com.bajins.demo.workersexecutors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 同步线程池
 */
@Configuration
public class ExecutorConfig {

    @Value("${thread.number}")
    private Integer threadNumber;

    /**
     * 在注入时注意使用Bean名称要一致
     *
     * @return
     */
    @Bean("poolTaskExecutor")
    public Executor asyncThreadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(threadNumber);
        // 设置线程队列大小
        executor.setQueueCapacity(threadNumber * 5);
        // 设置线程最大线程数量
        executor.setMaxPoolSize(threadNumber * 10);
        // 设置最大线程空闲时间,达到最大空闲时间则自动销毁
        executor.setKeepAliveSeconds(30);
        executor.setAllowCoreThreadTimeOut(true);
        /*
         * rejection-policy：当pool已经达到max size的时候，如何处理新任务
         * CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化
        executor.initialize();
        return executor;
    }

}
