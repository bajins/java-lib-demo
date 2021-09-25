package com.bajins.demo;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @see org.springframework.util
 * @see StringUtils 字符串工具类
 * @see Assert 断言工具类
 * @see ReflectionUtils 反射工具类
 * @see AopProxyUtils
 * @see AopUtils
 * @see ClassUtils
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
 * </br>
 * @see org.springframework.web.bind.ServletRequestUtils
 * @see RequestContextHolder
 * @see TransactionSynchronizationManager
 * @see LocaleContextHolder
 * @see ConfigurableListableBeanFactory Spring应用上下文环境
 * @see ApplicationContext https://www.cnblogs.com/pijunqi/p/14131648.html
 * @see WebApplicationContext
 * @see WebApplicationContextUtils
 * @see ApplicationContextAware
 * @see ApplicationObjectSupport
 * @see WebApplicationObjectSupport
 * @see BeanFactoryPostProcessor
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
    }
}
