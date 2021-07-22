package com.bajins.demo;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;


/**
 * logback用硬编码的方式调用了参数的toString方法
 * <pre>
 * 如果对象未自定义toString方法时：
 *      输出结果格式为：类名+@+内存地址
 *      如果是个数组对象时，格式为：[类名+@+内存地址
 * </pre>
 * 可以通过扩展Appender甚至是全局的过滤Filter来转换参数。
 * <pre>
 *      <configuration>
 *          <conversionRule conversionWord="m" converterClass="com.bajins.demo.LogJsonFilter"/>
 *      </configuration>
 * </pre>
 */
public class LogJsonFilter extends Filter<ILoggingEvent> {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getLoggerName().startsWith("com.bajins")) {
            Object[] params = event.getArgumentArray();
            for (int index = 0; index < params.length; index++) {
                Object param = params[index];
                if (!param.getClass().isPrimitive() && param instanceof Serializable) {
                    try {
                        params[index] = objectMapper.writeValueAsString(param);
                    } catch (JsonProcessingException e) {
                        params[index] = param.toString();
                    }
                }
            }
        }
        return FilterReply.ACCEPT;
    }
}