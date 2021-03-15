package com.bajins.demo;

import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * @see org.springframework.util
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
 */
public class SpringUtilLearning {
    public static void main(String[] args) throws IOException {
        String text = " \t ";
        System.out.println(StringUtils.isEmpty(text));
        System.out.println(StringUtils.hasText(text));

        Properties properties = PropertiesLoaderUtils.loadAllProperties("com/test/config/config.properties");
        System.out.println(properties.getProperty("key"));
    }
}
