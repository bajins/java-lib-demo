package com.bajins.demo.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author claer https://www.bajins.com
 * @program com.bajins.api.config
 * @create 2018-06-11 15:02
 */
// 在Job被执行结束后，将会更新JobDataMap
// 避免并发问题导致数据紊乱，应同时使用@DisallowConcurrentExecution注解
@PersistJobDataAfterExecution
// 同一时间将只有一个Job实例被执行
@DisallowConcurrentExecution
public class ThirtyMinutesJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println("调用任务");
    }


}