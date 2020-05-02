package com.bajins.demo.scheduling;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 动态配置、动态修改触发器
 */
@Component
public class SchedulingConfig implements SchedulingConfigurer {

    /**
     * @param scheduledTaskRegistrar
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        // 第一个为任务的具体逻辑实现，第二个为触发器，动态的定时任务则意味着Trigger需要动态获取
        scheduledTaskRegistrar.addTriggerTask(doTask(), getTrigger());
        //scheduledTaskRegistrar.setCronTasksList();
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
