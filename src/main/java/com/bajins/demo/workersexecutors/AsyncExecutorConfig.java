package com.bajins.demo.workersexecutors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 异步线程池配置
 */
@Configuration
@EnableAsync
public class AsyncExecutorConfig {

    @Value("${thread.number}")
    private Integer threadNumber;

    @Bean("async-executor")
    public ThreadPoolExecutor asyncExecutor() {
        int cpu = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(cpu, cpu << 2, 0L, TimeUnit.MILLISECONDS, new LinkedTransferQueue<>());
    }

    @Bean("async-redis")
    public ThreadPoolExecutor redisExecutor() {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedTransferQueue<>());
    }

    /**
     * <p>
     * 线程执行顺序
     * 当线程数小于核心线程数时,创建线程.
     * 当线程数大于等于核心线程数,且任务队列未满时,将任务放入任务队列;
     * 当线程数大于等于核心线程数,且任务队列已满.
     * 若线程数小于最大线程数,创建线程;
     * 若线程数等于最大线程数,抛出异常,拒绝任务;
     * </p>
     * <pre>
     * 1、Spring线程池默认值：
     *     * corePoolSize=1
     *     * queueCapacity=Integer.MAX_VALUE
     *     * maxPoolSize=Integer.MAX_VALUE
     *     * keepAliveTime=60s
     *     * allowCoreThreadTimeout=false
     *     * rejectedExecutionHandler=AbortPolicy()
     *
     * 2、如何来设置
     *     * 需要根据几个值来决定
     *         - tasks ：每秒的任务数，假设为100~1000
     *         - taskcost：每个任务花费时间，假设为0.1s
     *         - responsetime：系统允许容忍的最大响应时间，假设为1s
     *     * 做几个计算：corePoolSize = 每秒需要多少个线程处理？
     *     * threadcount = tasks/(1/taskcost) =tasks*taskcout =  (100~1000)*0.1 = 10~100 个线程。corePoolSize设置应该大于10
     *     * 根据8020原则，如果80%的每秒任务数小于400，那么corePoolSize设置为40即可
     *     	- queueCapacity = (coreSizePool/taskcost)*responsetime
     *     * 计算可得 queueCapacity = 40/0.1*1 = 400。意思是队列里的线程可以等待1s，超过了的需要新开线程来执行
     *     * 切记不能使用默认值，这样队列会很大，线程数只会保持在corePoolSize大小，当任务陡增时，不能新开线程来执行，响应时间会随之陡增。
     *     	- maxPoolSize = (max(tasks)- queueCapacity)/(1/taskcost)
     *     * 计算可得 maxPoolSize = (1000-400)/10 = 60
     *     * （最大任务数-队列容量）/每个线程每秒处理能力 = 最大线程数
     *         - rejectedExecutionHandler：根据具体情况来决定，任务不重要可丢弃，任务重要则要利用一些缓冲机制来处理
     *         - keepAliveTime和allowCoreThreadTimeout采用默认通常能满足
     * </pre>
     *
     * @return
     */
    @Bean("asyncThreadExecutor")
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

    /**
     * 异步线程使用：在调用当前方法时，Spring会自动使用多线程异步执行，必须在类上使用@EnableAsync才有效
     *
     * @return
     */
    @Async("asyncThreadExecutor")
    public String run() {
        System.out.println("测试线程启动了");
        return null;
    }

}
