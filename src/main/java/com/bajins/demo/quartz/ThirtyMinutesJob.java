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
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ThirtyMinutesJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println("调用任务");
    }


}