package com.bajins.demo.quartz;

import org.quartz.*;

/**
 * 每天早上八点job
 *
 * @author claer https://www.bajins.com
 * @program com.bajins.api.config
 * @description EightAmJob
 * @create 2018-06-12 18:10
 */
// 在Job被执行结束后，将会更新JobDataMap
// 避免并发问题导致数据紊乱，应同时使用@DisallowConcurrentExecution注解
@PersistJobDataAfterExecution
// 同一时间将只有一个Job实例被执行
@DisallowConcurrentExecution
public class EightAmJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("调用任务");
    }
}