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
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class EightAmJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("调用任务");
    }
}