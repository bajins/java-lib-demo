package com.bajins.demo.springboot;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;

import javax.servlet.http.HttpServletRequestWrapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * WebMvcConfigurationAdapter已经废弃，替代方式如下：
 * implements WebMvcConfigurer ：不会覆盖@EnableAutoConfiguration关于WebMvcAutoConfiguration的配置
 * 使用@EnableWebMvc=继承DelegatingWebMvcConfiguration=继承WebMvcConfigurationSupport
 * EnableWebMvc注解 + implements WebMvcConfigurer ：会覆盖@EnableAutoConfiguration关于WebMvcAutoConfiguration的配置
 * extends WebMvcConfigurationSupport ：会覆盖@EnableAutoConfiguration关于WebMvcAutoConfiguration的配置
 * extends DelegatingWebMvcConfiguration ：会覆盖@EnableAutoConfiguration关于WebMvcAutoConfiguration的配置
 * <p>
 * 需要注意会覆盖application.properties中关于WebMvcAutoConfiguration的设置，需要在自定义配置中实现
 *
 * @see WebMvcConfigurer
 * @see WebMvcConfigurerAdapter 过时
 * @see HttpServletRequestWrapper request增强器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private StringHttpMessageConverter stringHttpMessageConverter;
    @Autowired
    private MappingJackson2HttpMessageConverter httpMessageConverter;

    /*@Bean
    public ServletRegistrationBean resourceServlet() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        dispatcherServlet.setApplicationContext(applicationContext);
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(dispatcherServlet);
        servletRegistrationBean.addUrlMappings("*.css", "*.js", "*.ttf", "*.png", "*.jpg", "*.gif", "*.woff", "*
        .woff2");
        servletRegistrationBean.setLoadOnStartup(1);
        servletRegistrationBean.setName("resource");
        return servletRegistrationBean;
    }*/

    /**
     * 自定义全局http处理字符集编码
     * 通过注入，此方式在implements WebMvcConfigurer中无效
     * https://docs.spring.io/spring/docs/5.0.x/javadoc-api/org/springframework/http/converter/HttpMessageConverter.html
     *
     * @return
     */
    /*@Bean
    public HttpMessageConverter<String> responseBodyConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }*/

    /**
     * 自定义全局http处理字符集编码
     * 实现configureMessageConverters方法，添加了自定义converter后，list不为空，导致不会添加一些默认的converter
     * 在WebMvcConfigurationSupport#getMessageConverters方法中可以看到
     * https://docs.spring.io/spring/docs/5.0.x/javadoc-api/org/springframework/http/converter/HttpMessageConverter.html
     *
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        FastJsonConfig config = new FastJsonConfig();
        config.setSerializerFeatures(
                //保留Map 空的字段
                SerializerFeature.WriteMapNullValue,
                //将String 类型的null转成""
                SerializerFeature.WriteNullStringAsEmpty,
                //将Number类型的nutl转成0
                SerializerFeature.WriteNullNumberAsZero,
                //将List类型的null转成{]
                SerializerFeature.WriteNullListAsEmpty,
                //将Boolean类型的null转成false
                SerializerFeature.WriteNullBooleanAsFalse,
                //避免循环引用
                SerializerFeature.DisableCircularReferenceDetect);
        SerializeConfig serializeConfig = SerializeConfig.globalInstance;
        serializeConfig.put(Long.class, com.alibaba.fastjson.serializer.ToStringSerializer.instance);
        serializeConfig.put(Long.TYPE, ToStringSerializer.instance);
        config.setSerializeFilters((ValueFilter) (object, name, value) -> {
            //如果类型为Date且为null,返回空串
            if (value == null && object instanceof Date) {
                return "";
            }
            return value;
        });
        config.setSerializeConfig(serializeConfig);
        converter.setFastJsonConfig(config);
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        List<MediaType> mediaTypeList = new ArrayList<>();
        // 解决中文乱码问题,相当于在Controller上的 GetRequestMapping 中加了个属性produces="application/json;charset=UTF-8"
        // https://blog.csdn.net/a232884c/article/details/128544491
        // https://www.cnblogs.com/xuruiming/p/13283288.html
        mediaTypeList.add(MediaType.APPLICATION_JSON_UTF8);
        converter.setSupportedMediaTypes(mediaTypeList);
        converters.add(converter);
    }

    /**
     * 自定义全局http处理字符集编码
     * 实现extendMessageConverters方法不会出现与configureMessageConverters方法的同等问题
     *
     * @param converters
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 解决controller返回字符串中文乱码问题
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8);
            }
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                converters.remove(converter);
                converters.add(httpMessageConverter);
            }
        }
    }

    /**
     * 配置静态资源,避免静态资源请求被拦截问题：
     * No handler found for GET /swagger-ui.html
     * No mapping found for HTTP request with URI [/swagger-resources/configuration/ui] in DispatcherServlet with
     * name 'dispatcherServlet'
     *
     * @param registry
     * @return
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // url访问路径对应的资源路径
        if (!registry.hasMappingForPattern("/webjars/**")) {
            registry.addResourceHandler("/webjars/**").addResourceLocations(
                    "classpath:/META-INF/resources/webjars/");
        }
        if (!registry.hasMappingForPattern("/**")) {
            registry.addResourceHandler("/**").addResourceLocations("classpath:/META-INF/resources/"
                    , "classpath:/resources/", "classpath:/static/", "classpath:/public/");
        }
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
    }

    /**
     * 拦截器配置好后在这里添加注册
     * spring boot 2.x依赖的spring 5.x版本，静态资源也会执行自定义的拦截器
     * 相对于spring boot 1.5.x依赖的spring 4.3.x版本而言，针对资源的拦截器初始化时有区别
     *
     * @param registry
     * @return
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //registry.addInterceptor(new LoginInterceptor()).excludePathPatterns(Arrays.asList("/js/**","/css/**"));
        //registry.addInterceptor(new LoginHandlerInterceptor()).addPathPatterns("**").excludePathPatterns
        // ("/static/**");
    }

    /**
     * 添加视图
     *
     * @param registry
     * @return
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/ws").setViewName("/ws");
        //registry.addViewController("/").setViewName("forward:/index/index.html");
        //registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        //WebMvcConfigurer.super.addViewControllers(registry);
    }
}
