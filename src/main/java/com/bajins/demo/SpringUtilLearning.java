package com.bajins.demo;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.*;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.*;
import org.springframework.core.NamedInheritableThreadLocal;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.io.*;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.task.*;
import org.springframework.core.task.support.ConcurrentExecutorAdapter;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.data.util.Pair;
import org.springframework.format.datetime.standard.DateTimeContextHolder;
import org.springframework.objenesis.instantiator.util.UnsafeUtils;
import org.springframework.scheduling.concurrent.*;
import org.springframework.scheduling.quartz.LocalTaskExecutorThreadPool;
import org.springframework.scheduling.quartz.SimpleThreadPoolTaskExecutor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.util.*;

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * @see org.springframework.util
 * @see StringUtils 字符串工具类
 * @see Assert 断言工具类
 * @see ReflectionUtils 反射工具类
 * @see AopProxyUtils
 * @see AopUtils
 * @see ClassUtils
 * @see UnsafeUtils
 * @see DigestUtils
 * @see SerializationUtils
 * <br/>
 * @see org.springframework.beans JavaBean相关操作
 * @see BeanUtils
 * @see PropertyAccessorUtils
 * @see PropertyDescriptorUtils
 * @see BeanWrapperImpl
 * <br/>
 * @see org.springframework.core
 * @see BridgeMethodResolver 桥接方法分析器
 * @see GenericTypeResolver 范型分析器, 在用于对范型方法, 参数分析
 * @see NestedExceptionUtils
 * @see NamedThreadLocal
 * @see NamedInheritableThreadLocal
 * <br/>
 * @see org.springframework.core.annotation
 * @see AnnotationUtils 处理注解
 * <br/>
 * @see org.springframework.core.io
 * @see ClassPathResource
 * @see FileSystemResource
 * @see Resource
 * @see UrlResource
 * @see ServletContextResource
 * @see InputStreamResource
 * @see ByteArrayResource
 * @see EncodedResource
 * @see VfsResource
 * @see VfsUtils
 * <br/>
 * @see org.springframework.core.io.support
 * @see PathMatchingResourcePatternResolver
 * @see PropertiesLoaderUtils 加载Properties资源工具类,和Resource结合
 * <br/>
 * @see org.springframework.web.util
 * @see CookieGenerator
 * @see HtmlCharacterEntityDecoder
 * @see HtmlCharacterEntityReferences
 * @see HtmlUtils
 * @see HttpUrlTemplate
 * @see JavaScriptUtils
 * @see Log4jConfigListener 用listener的方式来配制log4j在web环境下的初始化
 * @see UriTemplate
 * @see UriUtils
 * @see WebUtils
 * @see WebAsyncUtils
 * </br>
 * @see org.springframework.web.bind.ServletRequestUtils
 * @see RequestContextHolder 子线程获取RequestAttributes https://blog.csdn.net/qq_25775675/article/details/125617310
 * @see RequestContextUtils
 * @see DateTimeContextHolder
 * @see TransactionSynchronizationManager
 * @see LocaleContextHolder
 * @see ConfigurableListableBeanFactory Spring应用上下文环境
 * @see BeanFactoryPostProcessor
 * @see ApplicationContext https://www.cnblogs.com/pijunqi/p/14131648.html
 * @see WebApplicationContext
 * @see WebApplicationContextUtils
 * @see ClassPathXmlApplicationContext
 * @see FileSystemXmlApplicationContext
 * @see GenericApplicationContext
 * @see AnnotationConfigApplicationContext
 * @see StaticApplicationContext
 * @see XmlWebApplicationContext
 * @see ApplicationContextAware
 * @see ApplicationObjectSupport
 * @see WebApplicationObjectSupport
 * </br> Interceptor拦截器，基于Java的反射机制（动态代理）实现
 * <pre> 调用顺序：
 * HandlerInterceptor.preHandle -> RequestBodyAdvice.supports -> RequestBodyAdvice.beforeBodyRead ->
 * RequestBodyAdvice.supports -> RequestBodyAdvice.afterBodyRead -> @RestController/@Controller ->
 * ResponseBodyAdvice.supports -> ResponseBodyAdvice.beforeBodyWrite -> HandlerInterceptor.postHandle ->
 * HandlerInterceptor.afterCompletion
 * </pre>
 * @see HandlerInterceptor 请求地址拦截器，调用顺序：preHandle -> Contorller -> postHandle -> afterCompletion
 * @see AsyncHandlerInterceptor
 * @see HandlerExceptionResolver
 * @see MethodInterceptor AOP项目中方法拦截器
 * @see LocaleChangeInterceptor
 * @see ThemeChangeInterceptor
 * @see WebRequestInterceptor
 * @see RequestBodyAdvice 配合@ControllerAdvice
 * @see ResponseBodyAdvice
 * </br> Filter 是Servlet过滤器，基于函数回调
 * @see Filter 过滤器 @ServletComponentScan @WebFilter @WebListener @WebServlet
 * @see OncePerRequestFilter
 * </br>
 * @see org.springframework.core.task
 * @see TaskExecutor
 * @see AsyncTaskExecutor
 * @see AsyncListenableTaskExecutor
 * @see SimpleAsyncTaskExecutor 非重用线程池，每次调用都会创建一个新的线程
 * @see SyncTaskExecutor 非异步执行
 * @see ConcurrentExecutorAdapter
 * @see ExecutorServiceAdapter
 * @see org.springframework.util.concurrent
 * @see ListenableFutureTask
 * @see org.springframework.scheduling.concurrent
 * @see ThreadPoolTaskExecutor
 * @see ThreadPoolTaskScheduler
 * @see TaskExecutorAdapter
 * @see ConcurrentTaskExecutor
 * @see ConcurrentTaskScheduler
 * @see org.springframework.jca.work
 * @see WorkManagerTaskExecutor
 * @see org.springframework.scheduling.quartz
 * @see LocalTaskExecutorThreadPool
 * @see SimpleThreadPoolTaskExecutor Quartz的SimpleThreadPool类的子类，它会监听Spring的生命周期回调
 */
public class SpringUtilLearning {

    /**
     * 获取所有非空的属性
     *
     * @param source
     * @return
     */
    public static String[] getPropertyNamesNotNull(Object source) {
        // BeanInfo wrappedSource = Introspector.getBeanInfo(source.getClass());
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = wrappedSource.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = wrappedSource.getPropertyValue(pd.getName());
            if (null != srcValue) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static String[] getPropertyNamesNotNullByStream(Object source) {
        // BeanInfo wrappedSource = Introspector.getBeanInfo(source.getClass());
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        // Java8 Stream获取非空属性
        String[] result = Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) != null)
                .toArray(String[]::new);
        return result;
    }

    public static void main(String[] args) throws IOException {
        String text = " \t ";
        System.out.println(StringUtils.isEmpty(text));
        System.out.println(StringUtils.hasText(text));

        Properties properties = PropertiesLoaderUtils.loadAllProperties("com/test/config/config.properties");
        System.out.println(properties.getProperty("key"));

        // Spring也是支持很友好的编码解决的~~~
        /*Properties properties = PropertiesLoaderUtils.loadProperties(new EncodedResource(
                new ClassPathResource("my.properties"), StandardCharsets.UTF_8));*/

        ClassPathResource classPathResource = new ClassPathResource("com/test/config/config.properties");

        Assert.notNull(text, "内容为空");
        Assert.hasText(text, "内容为空");


        // 使用org.springframework.wechatutil.StringUtils.countOccurrencesOf查找并统计子串出现在字符串中的次数
        int i = StringUtils.countOccurrencesOf("srcStr", "findStr");

        // 利用Spring Framework的StringUtils将逗号分隔的字符串转换为数组
        String[] strings = StringUtils.commaDelimitedListToStringArray("s,t,r");
        // 利用Spring Framework的StringUtils把list转String
        String s = StringUtils.collectionToDelimitedString(Arrays.asList(strings), ",");

        //org.springframework.beans.BeanUtils.copyProperties();
        //org.springframework.cglib.beans.BeanCopier.create().copy();
        //BeanUtils.copyProperties(Object source, Object target, String... ignoreProperties);

        // 二元组
        Pair<String, String> of = Pair.of("goodsBeans", "totalProperty");
        System.out.println(of.getFirst());

        /*WebApplicationContext wac = (WebApplicationContext)servletContext.getAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);*/

        // 线程池中创建 ThreadFactory 设置线程名称
        ThreadFactory springThreadFactory = new CustomizableThreadFactory("springThread-pool-");
        ExecutorService exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(10), springThreadFactory);
    }
}
