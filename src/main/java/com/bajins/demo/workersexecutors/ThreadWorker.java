package com.bajins.demo.workersexecutors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * 执行线程
 */
@Service
public class ThreadWorker {
    /**
     * 如在springMVC配置文件中配置该线程池信息,名字要与配置文件中bean的id一样
     * 如在springBoot中配置该线程池信息,名字要与@Bean中名称一样
     */
    private ThreadPoolTaskExecutor poolTaskExecutor;
    private TaskExecutor taskExecutor;

    @Autowired
    public void setPoolTaskExecutor(ThreadPoolTaskExecutor poolTaskExecutor) {
        this.poolTaskExecutor = poolTaskExecutor;
    }

    @Autowired
    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void run() {
        poolTaskExecutor.execute(new Thread(() -> {
            System.out.println(Thread.currentThread().getName());
        }));
        taskExecutor.execute(new Thread(() -> {
            System.out.println(Thread.currentThread().getName());
        }));
    }
}
