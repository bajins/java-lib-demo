package com.bajins.demo.quartz;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;

import javax.annotation.Resource;


/**
 * 重写的工厂对象
 */
public class CustomJobFactory extends AdaptableJobFactory {

    /**
     * AutowireCapableBeanFactory : 简单理解为Spring容器，是Spring容器Context的一个Bean对象管理工程。
     * 可以实现自动装配逻辑，和对象创建逻辑。
     * 是SpringIoC容器的一个重要组成部件。
     */
    @Resource
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        // 通过父类型中的方法，创建JobDetail对象。
        Object obj = super.createJobInstance(bundle);
        // 将JobDetail对象加入到Spring容器中，让Spring容器管理，并实现自动装配逻辑。
        this.autowireCapableBeanFactory.autowireBean(obj);
        return obj;
    }
}
