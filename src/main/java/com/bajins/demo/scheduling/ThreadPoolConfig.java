package com.bajins.demo.scheduling;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Bean("asyncServiceExecutor")
    public ThreadPoolTaskExecutor asyncRabbitTimeoutServiceExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //核心线程数
        threadPoolTaskExecutor.setCorePoolSize(5);
        //核心线程若处于闲置状态的话，超过一定的时间(KeepAliveTime)，就会销毁掉。
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
        //最大线程数
        threadPoolTaskExecutor.setMaxPoolSize(10);
        //配置队列大小
        threadPoolTaskExecutor.setQueueCapacity(300);
        //加入装饰器
        threadPoolTaskExecutor.setTaskDecorator(new ContextCopyingDecorator());
        //配置线程池前缀
        threadPoolTaskExecutor.setThreadNamePrefix("test-log-");
        //拒绝策略:只要线程池未关闭，该策略直接在调用者线程中串行运行被丢弃的任务，显然这样不会真的丢弃任务，但是可能会造成调用者性能急剧下降
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    static class ContextCopyingDecorator implements TaskDecorator {

        private static final ThreadLocal<Object> tl = new ThreadLocal<>();

        @Nonnull
        @Override
        public Runnable decorate(@Nonnull Runnable runnable) {
            //主流程
            Object res = tl.get();
            System.out.println("装饰前：" + res);
            //子线程逻辑
            return () -> {
                try {
                    //将变量重新放入到run线程中。
                    tl.set(res);
                    System.out.println("打印日志-开始");
                    runnable.run();
                } finally {
                    System.out.println("打印日志-结束");
                }
            };
        }
    }
}
