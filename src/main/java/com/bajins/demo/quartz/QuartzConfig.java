package com.bajins.demo.quartz;

import org.quartz.JobDataMap;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.*;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Quartz定时任务配置，如果不在此处配置任务，可使用QuartzManager调用Quartz API动态创建任务
 * https://github.com/sunjc/heroes-api
 * <p>
 * 在Spring环境中，创建一个类型的对象的时候，很多情况下，都是通过FactoryBean来间接创建的。
 * 如果有多个Job对象，定义多次方法。
 * 在JobDetailFactoryBean类型中，用于创建JobDetail对象的方法，其底层使用的逻辑是：Class.newInstance()
 * 也就是说，JobDetail对象不是通过Spring容器管理的。
 * 因为Spring容器不管理JobDetail对象，那么Job中需要自动装配的属性，就无法实现自动状态。如上Job的第10行会报空指针异常。
 * 解决方案是：将JobDetail加入到Spring容器中，让Spring容器管理JobDetail对象。需要重写Factory相关代码。实现Spring容器管理JobDetail。
 */
@Configuration
public class QuartzConfig {

    @Resource
    private DataSource dataSource;

    /**
     * 指定Job的实例名以及调用方法，创建Job对象
     * 但是无法在创建JobDetail时传递参数
     *
     * @return
     */
    @Bean
    public MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean() {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        // 指定触发器执行的bean名称
        bean.setTargetBeanName("thirtyMinutesJob");
        // 指定触发器执行的bean方法
        bean.setTargetMethod("executeInternal");
        return bean;
    }

    /**
     * 通过JobDetailFactoryBean实现的需要指定JobClass可通过Map导入参数，创建Job对象。
     *
     * @return
     */
    @Bean
    public JobDetailFactoryBean jobDetailFactoryBean() {
        JobDetailFactoryBean bean = new JobDetailFactoryBean();
        bean.setJobClass(EightAmJob.class);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("name", "sang");
        // 传递参数
        bean.setJobDataMap(jobDataMap);
        bean.setDurability(true);
        return bean;
    }

    /**
     * 创建调度器工厂bean对象
     *
     * @return
     * @throws IOException
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        factory.setSchedulerName("Cluster_Scheduler");
        factory.setDataSource(dataSource);
        factory.setApplicationContextSchedulerContextKey("applicationContext");
        // 设置调度器中的线程池。
        factory.setTaskExecutor(schedulerThreadPool());
        // 设置触发器
        factory.setTriggers(cronTriggerFactoryBean().getObject());
        // 设置quartz的配置信息
        factory.setQuartzProperties(quartzProperties());
        return factory;
    }

    /**
     * 读取quartz.properties配置文件
     *
     * @return
     * @throws IOException
     */
    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        // 在quartz.properties中的属性被读取并注入后再初始化对象
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    @Bean
    public SimpleTriggerFactoryBean simpleTriggerFactoryBean() {
        //使用SimpleTriggerFactoryBean创建
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        //首先设置JobDetail
        simpleTriggerFactoryBean.setJobDetail(methodInvokingJobDetailFactoryBean().getObject());
        //其次配置任务次数
        simpleTriggerFactoryBean.setRepeatCount(3);
        //然后配置任务启动延时时间
        simpleTriggerFactoryBean.setStartDelay(1000);
        //最后配置任务的时间间隔
        simpleTriggerFactoryBean.setRepeatInterval(2000);
        return simpleTriggerFactoryBean;
    }

    /**
     * 创建trigger factory bean对象
     *
     * @return
     */
    @Bean
    public CronTriggerFactoryBean cronTriggerFactoryBean() {
        //在CronTriggerFactoryBean中主要配置JobDetail和CronExpression
        CronTriggerFactoryBean bean = new CronTriggerFactoryBean();
        bean.setJobDetail(jobDetailFactoryBean().getObject());
        bean.setCronExpression("* * * * * ?");
        return bean;
    }

    /**
     * 创建调度器工厂bean对象
     *
     * @return
     */
    @Bean
    public SchedulerFactoryBean schedulerFactory() {
        //通过SchedulerFactoryBean创建SchedulerFactory
        SchedulerFactoryBean bean = new SchedulerFactoryBean();
        SimpleTrigger simpleTrigger = simpleTriggerFactoryBean().getObject();
        Trigger trigger = cronTriggerFactoryBean().getObject();
        //setTriggers中参数为Trigger的class
        bean.setTriggers(simpleTrigger, trigger);
        return bean;
    }

    /**
     * 创建一个调度器的线程池。
     *
     * @return
     */
    @Bean
    public Executor schedulerThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(15);
        executor.setMaxPoolSize(25);
        executor.setQueueCapacity(100);
        return executor;
    }
}
