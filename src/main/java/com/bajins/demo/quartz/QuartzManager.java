package com.bajins.demo.quartz;


import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Quartz定时任务管理类
 * <pre>
 * 核心接口有 https://blog.csdn.net/QXC1281/article/details/68924140
 * Scheduler – (调度器)与scheduler交互的主要API；
 * Job – (作业)你通过scheduler执行任务，你的任务类需要实现的接口；
 * JobDetail – (作业实例)定义Job的实例；
 * Trigger – (触发器)触发Job的执行；
 *      SimpleTrigger :简单触发器
 *      CalendarIntervalTrigger:日历触发器
 *      CronTrigger:Cron表达式触发器
 *      DailyTimeIntervalTrigger:日期触发器
 * JobBuilder – 定义和创建JobDetail实例的接口;
 * TriggerBuilder – 定义和创建Trigger实例的接口；
 * </pre>
 *
 * @author claer https://www.bajins.com
 * @create 2018-12-19 00:46
 */
@Component
public class QuartzManager {

    private static String JOB_GROUP_NAME = "defaultGroup";
    private static String TRIGGER_GROUP_NAME = "defaultTrigger";

    @Resource
    private Scheduler scheduler;


    /**
     * 恢复所有的PAUSED、ERROR状态的任务
     *
     * @throws SchedulerException
     */
    public void resumeJobs() throws SchedulerException {
        List<String> jobGroupNames = scheduler.getJobGroupNames();
        for (String jobGroupName : jobGroupNames) {
            Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(jobGroupName));
            for (TriggerKey triggerKey : triggerKeys) {
                Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
                if (Trigger.TriggerState.PAUSED.equals(triggerState) || Trigger.TriggerState.ERROR.equals(triggerState)) {
                    scheduler.resumeJobs(GroupMatcher.jobGroupEquals(jobGroupName));
                }
            }
        }
    }


    /**
     * 添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
     *
     * @param jobName  任务名
     * @param jobClass 任务类名
     * @param time     时间间隔 每隔多少时间执行一次 单位秒 如60*60 = 1小时
     */
    public void saveJobSimple(String jobName, Class<? extends Job> jobClass, Integer time) throws SchedulerException {
        saveJobSimple(jobName, JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME, jobClass, time, true);
    }

    public void saveJobSimple(String jobName, Class<? extends Job> jobClass, Integer time, boolean firstRun)
            throws SchedulerException {
        saveJobSimple(jobName, JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME, jobClass, time, firstRun);
    }

    /**
     * 添加一个定时任务
     *
     * @param jobName          任务名
     * @param jobGroupName     任务组名
     * @param triggerName      触发器名
     * @param triggerGroupName 触发器组名
     * @param jobClass         任务类名
     * @param time             时间间隔 每隔多少时间执行一次 单位秒 如60*60 = 1小时
     * @throws SchedulerException
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void saveJobSimple(String jobName, String jobGroupName, String triggerName, String triggerGroupName,
                              Class<? extends Job> jobClass, Integer time, boolean firstRun) throws SchedulerException {

        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, triggerGroupName);
        SimpleTrigger trigger = (SimpleTrigger) scheduler.getTrigger(triggerKey);
        // 不存在，创建一个
        if (null == trigger) {
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();
            // 按新的cronExpression表达式构建一个新的trigger
            TriggerBuilder s = TriggerBuilder.newTrigger().withIdentity(triggerName, triggerGroupName);
            if (!firstRun) {
                s.startAt(Date.from(LocalDateTime.now().plusSeconds(time).toInstant(ZoneOffset.ofHours(8))));
            }
            trigger = (SimpleTrigger) s.withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(time))
                    //.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(time).repeatForever())
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
            // 启动调度
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
            // 获取触发器状态
            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            // 判断触发器状态是否为暂停
            if (Trigger.TriggerState.PAUSED.equals(triggerState)) {
                // 如果触发器为暂停就恢复启动
                scheduler.resumeTrigger(trigger.getKey());
            }
        } else {
            // Trigger已存在，那么更新相应的定时设置
            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
                    .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(time))
                    //.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(time).repeatForever())
                    .build();
            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }


    /**
     * 添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
     *
     * @param jobName        任务名
     * @param jobClass       任务类名
     * @param cronExpression 时间设置，参考quartz说明文档
     * @param data           定时任务所带参数数据
     * @param endDate        定时任务生命周期结束时间
     */
    public void saveJobCron(String jobName, Class<? extends Job> jobClass, String cronExpression, JobDataMap data,
                            Date endDate) throws SchedulerException {
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("cron表达式不正确");
        }
        saveJobCron(jobName, JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME, jobClass, cronExpression, data, endDate);
    }

    /**
     * 添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
     *
     * @param jobName        任务名
     * @param jobClass       任务类名
     * @param cronExpression 时间设置，参考quartz说明文档
     */
    public void saveJobCron(String jobName, Class<? extends Job> jobClass, String cronExpression) throws SchedulerException {
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("cron表达式不正确");
        }
        saveJobCron(jobName, JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME, jobClass, cronExpression);
    }

    /**
     * 添加一个定时任务
     *
     * @param jobName          任务名
     * @param jobGroupName     任务组名
     * @param triggerName      触发器名
     * @param triggerGroupName 触发器组名
     * @param jobClass         任务类名
     * @param cronExpression   时间设置，参考quartz说明文档
     * @param data             对应定时认为中所涉及的数据
     * @param endDate          定时任务周期结束时间
     * @throws SchedulerException
     */
    public void saveJobCron(String jobName, String jobGroupName, String triggerName, String triggerGroupName,
                            Class<? extends Job> jobClass, String cronExpression, JobDataMap data, Date endDate) throws SchedulerException {
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("cron表达式不正确");
        }
        // 表达式调度构建器
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, triggerGroupName);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        // 不存在，创建一个
        if (null == trigger) {
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).setJobData(data)
                    .build();
            // 按新的cronExpression表达式构建一个新的trigger
            trigger = TriggerBuilder.newTrigger().withIdentity(triggerName, triggerGroupName)
                    .withSchedule(scheduleBuilder).endAt(endDate).build();
            scheduler.scheduleJob(jobDetail, trigger);
            // 启动调度
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
            // 获取触发器状态
            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            // 判断触发器状态是否为暂停
            if (Trigger.TriggerState.PAUSED.equals(triggerState)) {
                // 如果触发器为暂停就恢复启动
                scheduler.resumeTrigger(trigger.getKey());
            }
        } else {
            // Trigger已存在，那么更新相应的定时设置
            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder)
                    .startAt(new Date()).endAt(endDate).build();
            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }

    /**
     * 添加一个定时任务
     *
     * @param jobName          任务名
     * @param jobGroupName     任务组名
     * @param triggerName      触发器名
     * @param triggerGroupName 触发器组名
     * @param jobClass         任务类名
     * @param cronExpression   时间设置，参考quartz说明文档
     * @throws SchedulerException
     */
    public void saveJobCron(String jobName, String jobGroupName, String triggerName, String triggerGroupName,
                            Class<? extends Job> jobClass, String cronExpression) throws SchedulerException {
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("cron表达式不正确");
        }
        // 表达式调度构建器
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, triggerGroupName);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        // 不存在，创建一个
        if (null == trigger) {
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();
            // 按新的cronExpression表达式构建一个新的trigger
            trigger = TriggerBuilder.newTrigger().withIdentity(triggerName, triggerGroupName)
                    .withSchedule(scheduleBuilder).build();
            scheduler.scheduleJob(jobDetail, trigger);
            // 启动调度
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
            // 获取触发器状态
            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            // 判断触发器状态是否为暂停
            if (Trigger.TriggerState.PAUSED.equals(triggerState)) {
                // 如果触发器为暂停就恢复启动
                scheduler.resumeTrigger(trigger.getKey());
            }
        } else {
            // Trigger已存在，那么更新相应的定时设置
            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }

    /**
     * 修改一个任务的触发时间
     *
     * @param triggerName      触发器名
     * @param triggerGroupName 触发器组名
     * @param cron             时间设置，参考quartz说明文档
     */
    public void modifyJobTime(String triggerName, String triggerGroupName, String cron) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if (trigger == null) {
            return;
        }
        String oldTime = trigger.getCronExpression();
        if (!oldTime.equalsIgnoreCase(cron)) {
            // 触发器
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            // 触发器名,触发器组
            triggerBuilder.withIdentity(triggerName, triggerGroupName);
            triggerBuilder.startNow();
            // 触发器时间设定
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
            // 创建Trigger对象
            trigger = (CronTrigger) triggerBuilder.build();
            // 方式一 ：修改一个任务的触发时间
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }

    /**
     * 修改一个任务的触发时间：先删除，然后在创建一个新的Job
     *
     * @param jobName
     * @param jobGroupName
     * @param triggerName      触发器名
     * @param triggerGroupName 触发器组名
     * @param cron             时间设置，参考quartz说明文档
     */
    public void removeAndCreateJob(String jobName, String jobGroupName, String triggerName,
                                   String triggerGroupName, String cron) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if (trigger == null) {
            return;
        }
        String oldTime = trigger.getCronExpression();
        if (!oldTime.equalsIgnoreCase(cron)) {
            /** 方式二：先删除，然后在创建一个新的Job  */
            JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(jobName, jobGroupName));
            Class<? extends Job> jobClass = jobDetail.getJobClass();
            // 移除定时任务
            removeJob(jobName, jobGroupName, triggerName, triggerGroupName);

            // 添加定时任务
            saveJobCron(jobName, jobGroupName, triggerName, triggerGroupName, jobClass, cron);
        }
    }

    /**
     * 移除一个任务(使用默认的任务组名，触发器名，触发器组名)
     *
     * @param jobName
     * @throws SchedulerException
     */
    public void removeJob(String jobName) throws SchedulerException {
        removeJob(jobName, JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME);
    }

    /**
     * 移除一个任务
     *
     * @param jobName
     * @param jobGroupName
     * @param triggerName
     * @param triggerGroupName
     * @throws SchedulerException
     */
    public void removeJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName)
            throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
        scheduler.pauseTrigger(triggerKey);// 停止触发器
        scheduler.unscheduleJob(triggerKey);// 移除触发器
        scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName));// 删除任务
    }

    /**
     * 移除一个任务组下的所有任务
     *
     * @param groupName
     * @throws SchedulerException
     * @author: https://www.bajins.com
     * @date: 2018年12月18日 下午5:19:31
     */
    public void removeGroupJobs(String groupName) throws SchedulerException {

        GroupMatcher<TriggerKey> triggerGroupEquals = GroupMatcher.triggerGroupEquals(groupName);
        scheduler.pauseTriggers(triggerGroupEquals);// 停止触发器

        Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(triggerGroupEquals);
        List<TriggerKey> triggerKeyList = new ArrayList<TriggerKey>();
        triggerKeyList.addAll(triggerKeys);
        scheduler.unscheduleJobs(triggerKeyList);// 移除触发器

        GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(groupName);
        Set<JobKey> jobkeySet = scheduler.getJobKeys(matcher);
        List<JobKey> jobkeyList = new ArrayList<JobKey>();
        jobkeyList.addAll(jobkeySet);
        scheduler.deleteJobs(jobkeyList);// 删除任务
    }


    /**
     * 查询所有的job
     */
    public List<Map<String, Object>> getAllJobs() throws SchedulerException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String groupName : scheduler.getJobGroupNames()) {
            list.addAll(getGroupJobs(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))));
        }
        return list;
    }

    /**
     * 查询任务组下面的所有任务
     *
     * @param jobKeys 任务key
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    public List<Map<String, Object>> getGroupJobs(Set<JobKey> jobKeys) throws SchedulerException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (JobKey jobKey : jobKeys) {
            List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
            Map<String, Object> map = new HashMap<>();
            map.put("jobName", jobKey.getName());
            map.put("jobGroup", jobKey.getGroup());
            map.put("nextFireTime", triggers.get(0).getNextFireTime());
            list.add(map);
        }
        return list;
    }

    public void addJobTest(String id, String jobGroup, Class clazz, Map<String, Object> data, Date startAt,
                           Date endAt) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(clazz) // 创建builder，(jobDetail的预准备对象)
                // 通过builder调用withIdentity()去设置builder的名字和分组,最后通过build()方法获得一个jobDetail对象
                .withIdentity(id, jobGroup).build();
        jobDetail.getJobDataMap().putAll(data);

        // 定义一个触发器trigger对象，用来执行jobDetail
        SimpleTrigger trigger = TriggerBuilder.newTrigger() //创建一个触发器trigger对象
                // 设置触发器的名字和分组
                .withIdentity(id, jobGroup)
                // 设置以哪种方式执行JobDetail：
                // 一、SimpleScheduleBuilder 简单任务的重复执行SimpleScheduleBuilder.repeatSecondlyForever(5)
                // 二、CronTrigger 按日历触发任务CronScheduleBuilder.cronSchedule("0 17 1 * * ?")
                // CalendarIntervalScheduleBuilder.calendarIntervalSchedule().withInterval(2, DateBuilder.IntervalUnit.SECOND)
                // 以及执行一次JobDetail的间隔时间,以及执行到什么时候
                .forJob(jobDetail)
                // 每隔5分钟执行一次,永远重复不限制次数执行,失效之后，再启动马上执行
                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(1))
                //设置触发器开始执行JobDetail的起始时间，还有startNow()立即执行
                .startAt(startAt)
                // 结束时间 endAt（“结束的时间”），实现在任务执后自动销毁任务
                .endAt(endAt)
                // 最终获得一个Trigger对象
                .build();
        try {
            // 调度容器设置JobDetail和Trigger
            scheduler.scheduleJob(jobDetail, trigger);

            // 启动
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
            // 获取触发器状态
            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            // 判断触发器状态是否为暂停
            if (Trigger.TriggerState.PAUSED.equals(triggerState)) {
                // 如果触发器为暂停就恢复启动
                scheduler.resumeTrigger(trigger.getKey());
            }
        } catch (SchedulerException e) {
            throw e;
        }
    }


    public static void main(String[] args) {
        SchedulerFactory sf = new StdSchedulerFactory();
        try {
            // 创建一个调度对象
            Scheduler scheduler = sf.getScheduler();

            // 启动一个调度对象
            scheduler.start();

            // 检查调度是否启动
            scheduler.isStarted();

            // 添加调度的job信息
            //scheduler.scheduleJob("jobdetail", trigger);

            // 添加相关的触发器
            //scheduler.scheduleJob(trigger);

            // 添加多个job任务
            //scheduler.scheduleJobs(triggersAndJobs, true);

            // 停止调度Job任务
            //scheduler.unscheduleJob(triggerkey);

            // 停止调度多个触发器相关的job
            //scheduler.unscheduleJobs(triggerKeylist);

            // 重新恢复触发器相关的job任务
            //scheduler.rescheduleJob(triggerkey, trigger);

            // 添加相关的job任务
            //scheduler.addJob(jobdetail, true);

            // 删除相关的job任务
            //scheduler.deleteJob(jobkey);

            // 删除相关的多个job任务
            //scheduler.deleteJobs(jobKeys);

            //scheduler.triggerJob(jobkey);

            //scheduler.triggerJob("jobkey", jobdatamap);

            // 停止一个job任务
            //scheduler.pauseJob(jobkey);

            // 停止多个job任务
            //scheduler.pauseJobs(groupmatcher);

            // 停止多个job任务
            scheduler.pauseJobs(GroupMatcher.jobGroupEquals("groupName"));

            // 停止使用相关的触发器
            //scheduler.pauseTrigger(triggerkey);

            // 恢复相关的job任务
            //scheduler.pauseJob(jobkey);

            // 恢复多个任务
            //scheduler.resumeJobs(matcher);

            // 查询任务组下面的所有任务
            //getGroupJobs(scheduler.getJobKeys(GroupMatcher.jobGroupEquals("groupName")));

            // 判断是否是有效的cron表达式
            CronExpression.isValidExpression("cronExpression");

            // 停止任务
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }

            // 恢复多个任务
            scheduler.resumeJobs(GroupMatcher.jobGroupEquals("groupName"));

            //scheduler.resumeTrigger(triggerkey);

            //scheduler.resumeTriggers(groupmatcher);

            // 方法描述: 恢复任务
            scheduler.resumeJob(JobKey.jobKey("jobName"));

            // 暂停调度中所有的job任务
            scheduler.pauseAll();

            // 恢复调度中所有的job的任务
            scheduler.resumeAll();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}