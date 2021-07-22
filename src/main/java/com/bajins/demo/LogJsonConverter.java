package com.bajins.demo;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.helpers.MessageFormatter;

import java.util.stream.Stream;

/**
 * logback用硬编码的方式调用了参数的toString方法
 * <pre>
 * 如果对象未自定义toString方法时：
 *      输出结果格式为：类名+@+内存地址
 *      如果是个数组对象时，格式为：[类名+@+内存地址
 * </pre>
 * 可以通过MessageConverter来转换参数。
 * <pre>
 *      <configuration>
 *          <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
 *              <filter class="com.bajins.demo.LogJsonConverter"/>
 *          <appender>
 *      </configuration>
 * </pre>
 */
public class LogJsonConverter extends MessageConverter {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convert(ILoggingEvent event) {
        try {
            Object[] objects = Stream.of(event.getArgumentArray()).map(t -> {
                String str;
                if (t instanceof String) {
                    // String类型直接打印
                    str = t.toString();
                } else {
                    try {
                        str = objectMapper.writeValueAsString(t);
                    } catch (JsonProcessingException e) {
                        str = t.toString();
                    }
                }
                return str;
            }).toArray();
            return MessageFormatter.arrayFormat(event.getMessage(), objects).getMessage();
        } catch (Exception e) {
            return event.getMessage();
        }
    }

}
