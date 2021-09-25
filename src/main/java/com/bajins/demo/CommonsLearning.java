package com.bajins.demo;


import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.comparators.ComparableComparator;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.text.StringEscapeUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;


/**
 * commons-beanutils:commons-beanutils
 * commons-codec:commons-codec
 * commons-collections:commons-collections
 * commons-fileupload:commons-fileupload
 * commons-io:commons-io
 * commons-logging:commons-logging
 * org.apache.commons:commons-collections4
 * org.apache.commons:commons-compress
 * org.apache.commons:commons-csv
 * org.apache.commons:commons-lang3
 * org.apache.commons:commons-math3
 * org.apache.commons:commons-text
 * org.apache.httpcomponents:httpclient
 * org.apache.httpcomponents:httpcore
 * org.apache.httpcomponents:httpmime
 * org.apache.logging.log4j:log4j-api
 * org.apache.logging.log4j:log4j-to-slf4j
 * org.apache.poi:poi
 * org.apache.poi:poi-ooxml
 * org.apache.poi:poi-ooxml-schemas
 * org.apache.tomcat.embed:tomcat-embed-core
 * org.apache.tomcat.embed:tomcat-embed-el
 * org.apache.tomcat.embed:tomcat-embed-websocket
 * org.apache.xmlbeans:xmlbeans
 *
 * @see PropertyUtils 和BeanUtils不同在于：运行getProperty、setProperty操作时，没有类型转换，使用属性的原有类型或者包装类
 * @see BeanUtils
 * @see org.apache.commons.collections4
 */
public class CommonsLearning {


    public static void main(String[] args) {
        try {
            // map转bean
            BeanUtils.populate("obj", new HashMap<>());

            /**
             * 对象拷贝
             */
            // No value specified for 'Date'：复制对象内存在Date类型的属性，但是Date没有初始值，因此需要提前设置初始值
            ConvertUtils.register(new DateConverter(null), java.util.Date.class);
            //org.apache.commons.beanutils.BeanUtils.copyProperties();
            //org.apache.commons.beanutils.PropertyUtils.copyProperties();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        // Html 转码.
        String html = StringEscapeUtils.escapeHtml4("html");
        // Html 解码.
        String htmlEscaped = StringEscapeUtils.unescapeHtml4("htmlEscaped");
        // Xml 转码.
        String xml = StringEscapeUtils.escapeXml11("xml");
        // Xml 解码.
        String xmlEscaped = StringEscapeUtils.unescapeXml("xmlEscaped");


        // 使用Apache Collections对List集合中的泛型中的bean某一个字段排序
        //Comparator<?> mycmp = ComparableComparator.getInstance(); // collections4之前版本的使用方式
        Comparator<?> mycmp = new ComparableComparator();
        // 允许null
        mycmp = ComparatorUtils.nullLowComparator(mycmp);
        // 逆序，默认为正序
        mycmp = ComparatorUtils.reversedComparator(mycmp);

        List<Integer> intList = Lists.newArrayList();
        intList.add(1);
        intList.add(2);
        intList.add(3);
        intList.add(4);

        intList.sort(new BeanComparator("fieldName", mycmp));

        // 将一个list按三个一组分成N个小的list，
        // 没有对应的Iterable.partions方法，类似guava那样，partition后的结果同样是原集合的视图
        List<List<Integer>> subSets = ListUtils.partition(intList, 3);
        List<Integer> lastPartition = subSets.get(2);

        try { // https://cloud.tencent.com/developer/article/1497667
            Configurations configs = new Configurations();

            // 设置编码，此处的实际是个PropertiesConfiguration，它默认采用的是`ISO-8859-1`所以中文乱码~
            // 注意：这个前提是你的properties文件是utf-8编码的~~~
            FileBasedConfigurationBuilder.setDefaultEncoding(PropertiesConfiguration.class, "UTF-8");
            // 每个Configuration代表这一个配置文件~（依赖beanutils这个jar）
            Configuration config = configs.properties("my.properties");

            // 采用Builder模式处理更为复杂的一些场景   比如把逗号分隔的字符串解析到数组、解析到list、前后拼接字符串等等操作
            // 其实你直接configs.properties(...)它的内部原理也是builder模式~
            /*FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.propertiesBuilder("my.properties");
            builder.addEventListener();
            builder.getConfiguration();
            builder.getFileHandler();*/

            // 遍历
            Iterator<String> keys = config.getKeys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = config.getString(key);
                System.out.println(key + " = " + value);
            }
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

    }
}
