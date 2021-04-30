package com.bajins.demo;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.springframework.util.StringUtils;

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 各种模板
 */
public class TemplateLearning {

    /**
     * 自定义渲染模板
     *
     * @param template 模版
     * @param params   参数
     * @return
     */
    public static String processTemplate(String template, Map<String, Object> params) {
        if (!StringUtils.hasText(template)) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        Matcher m = Pattern.compile("\\$\\{\\w+\\}").matcher(template);
        while (m.find()) {
            String param = m.group();
            Object value = params.get(param.substring(2, param.length() - 1));
            m.appendReplacement(sb, value == null ? "" : value.toString());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 使用Velocity作为变量替换引擎，渲染模板
     *
     * @param template
     * @param params
     * @return
     */
    public static String processVelocity(String template, Map<String, Object> params) {
        if (!StringUtils.hasText(template)) {
            return null;
        }
        Context context = new VelocityContext();
        //拼装velocity用模板参数
        Set<String> keys = params.keySet();
        for (String k : keys) {
            context.put(k, params.get(k));
        }
        StringWriter sw = new StringWriter();
        try {
            Velocity.evaluate(context, sw, "velocity", template);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sw.toString();
    }

    /**
     * Freemarker渲染模板
     *
     * @param template 模版
     * @param params   参数
     * @return
     */
    public static String processFreemarker(String template, Map<String, Object> params) {
        if (!StringUtils.hasText(template)) {
            return null;
        }
        try {
            StringTemplateLoader stl = new StringTemplateLoader();
            stl.putTemplate("content", template);
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_20);
            cfg.setTemplateLoader(stl);
            /*<bean id="freemarkerConfig"
                class="org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer">
              <property name="freemarkerSettings">
                <props>
                  <prop key="classic_compatible">true</prop>
                </props>
              </property>
            </bean>*/
            //spring.freemarker.settings.classic_compatible=true
            // 在ftl前加入<!--#setting classic_compatible=true-->;
            // <#escape x as x!"">null替换为空字符串</#escape> <#noescape>不处理null</#noescape>
            /* ${user?if_exists} ${user!''} ${user!} ${user?default('')} ${user???string(user,'')} */
            cfg.setClassicCompatible(true);// 为null则替换为空字符串
            cfg.setTimeZone(TimeZone.getTimeZone("GMT> 08:00"));// 获取东八区时间
            cfg.setTimeFormat("HH:mm:ss.SSS");// 时间格式化
            cfg.setDateFormat("yyyy-MM-dd");// 日期格式化
            cfg.setDateTimeFormat("yyyy-MM-dd HH:mm:ss.SSS");// 日期时间格式化
            Template tpl = cfg.getTemplate("content");
            StringWriter writer = new StringWriter();
            tpl.process(params, writer);
            return writer.toString();
            /*Environment env = tpl.createProcessingEnvironment(params, writer);
            env.setClassicCompatible(true);// 为null则替换为空字符串
            env.setCustomAttribute("paramMap", params);// 自定义属性
            env.process();
            return writer.toString();*/
        } catch (Exception e) {
            return null;
        }
    }
}
