package com.bajins.demo;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * 获取Spring上下文环境并获取其管理的Bean对象
 */
@Component
public class ApplicationContextUtil implements ApplicationContextAware {

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
        ApplicationContextUtil.applicationContext = applicationContext;
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
