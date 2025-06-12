package com.bajins.demo;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.context.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.validation.constraints.NotNull;

/**
 * 获取Spring上下文环境并获取其管理的Bean对象
 *
 * @see InitializingBean 
 * @see BeanPostProcessor Bean后置处理器
 * @see InstantiationAwareBeanPostProcessor
 * @see InstantiationAwareBeanPostProcessorAdapter
 * @see SmartInstantiationAwareBeanPostProcessor
 * @see MergedBeanDefinitionPostProcessor
 * @see DestructionAwareBeanPostProcessor
 * @see BeanFactoryPostProcessor Spring应用上下文环境，获取Bean
 * @see BeanDefinitionRegistryPostProcessor 
 * @see ConfigurableListableBeanFactory
 * @see AbstractApplicationContext https://blog.csdn.net/qq_38257958/article/details/134753005
 * @see ApplicationContext https://www.cnblogs.com/pijunqi/p/14131648.html
 * @see ConfigurableApplicationContext
 * @see ContextLoader
 * @see WebApplicationContext
 * @see WebApplicationContextUtils
 * @see ClassPathXmlApplicationContext
 * @see FileSystemXmlApplicationContext
 * @see GenericApplicationContext
 * @see AnnotationConfigApplicationContext
 * @see StaticApplicationContext
 * @see XmlWebApplicationContext
 * @see ApplicationContextAwareProcessor Bean后置处理器
 * @see ApplicationContextAware
 * @see ApplicationObjectSupport
 * @see WebApplicationObjectSupport
 * @see BeanNameAware
 * @see BeanFactoryAware
 * @see BeanClassLoaderAware
 * @see EmbeddedValueResolverAware
 * @see EnvironmentAware
 * @see MessageSourceAware
 */
@Component
public class SpringAppContextUtils implements ApplicationContextAware {

    /**
     * Spring上下文
     */
    private static ApplicationContext applicationContext;

    /**
     * 将spring上下文同步到当前的applicationContext
     *
     * @param applicationContext 上下文
     * @throws BeansException bean异常
     */
    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringAppContextUtils.applicationContext = applicationContext;
    }

    /**
     * 获取已注入到spring上下文中的对象
     *
     * @param beanName 类名称
     * @param t        返回指定的对象
     * @param <T>
     * @return
     * @throws BeansException bean异常
     */
    public static <T> @NotNull T getBean(String beanName, Class<T> t) throws BeansException {
        /*//ServletContext servletContext = httpServletRequest.getSession().getServletContext();
        ServletContext servletContext = ContextLoader.getCurrentWebApplicationContext().getServletContext();
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        T sv = context.getBean("testService");

        ApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        T sv = context.getBean("testService");*/
        return (T) applicationContext.getBean(beanName);
    }
}
