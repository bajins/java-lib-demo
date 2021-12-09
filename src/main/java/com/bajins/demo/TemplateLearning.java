package com.bajins.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.StringWriter;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 各种模板
 *
 * @see VelocityContext
 * @see Template
 * @see StringTemplateLoader
 * @see RestTemplate
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
 * @see HIbernateTemplate
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
     * https://freemarker.apache.org/docs/ref_builtins_date.html
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
            cfg.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));// 获取东八区时间
            cfg.setTimeFormat("HH:mm:ss.SSS");// 时间格式化
            cfg.setDateFormat("yyyy-MM-dd");// 日期格式化
            cfg.setDateTimeFormat("yyyy-MM-dd HH:mm:ss.SSS");// 日期时间格式化
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            //Template tpl = new Template("content", template, cfg);
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

    /**
     * 在spring中使用aop注入时需注意：spring可能有RestTemplate的默认配置（请求头等），导致在某些情况下会有差异，所以最好是自己进行初始化
     */
    public static void restTemplate() throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        // 表单
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("shopid", "1");

        HttpEntity<MultiValueMap<String, String>> multiValueMapHttpEntity = new HttpEntity<>(map, headers);

        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity("http://posturl",
                multiValueMapHttpEntity, String.class);
        if (stringResponseEntity.getStatusCode() != HttpStatus.OK) { // 请求异常
            return;
        }
        System.out.println(stringResponseEntity);

        // json
        MediaType type = MediaType.parseMediaType(MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8");
        headers.setContentType(type);
        //headers.setAccept(Collections.singletonList(type));
        headers.add("Accept", type.toString());
        //headers.add("Accept", MediaType.APPLICATION_JSON.toString());

        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(map);
        HttpEntity<String> formEntity = new HttpEntity<>(s, headers);

        String result = restTemplate.postForObject("http://posturl", formEntity, String.class);


        //Type[] genericParameterTypes = thisMethod.getGenericParameterTypes(); // String url,Class<T> clazz
        //Type[] actualTypeArguments = ((ParameterizedType) genericParameterTypes[1]).getActualTypeArguments(); // T
        //Type ttype = actualTypeArguments[0]; // T.class
        //ParameterizedTypeReference<T> objectParameterizedTypeReference = ParameterizedTypeReference.forType(ttype);

        ParameterizedTypeReference<Map<String, Object>> parameterizedTypeReference =
                new ParameterizedTypeReference<Map<String, Object>>() {
                };
        String url = "https://test.com/tags/{1}/test?page={2}&count={3}&order=new&before_timestamp=";
        ResponseEntity<Map<String, Object>> exchange = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<String>(headers), parameterizedTypeReference, "test", 1, 100);

        URI uri = UriComponentsBuilder.fromHttpUrl("http://posturl").build(true).toUri();
        RequestEntity<Void> accept = RequestEntity.get(uri).header("Accept", type.toString()).build();
        ResponseEntity<Map<String, Object>> exchange1 = restTemplate.exchange(accept, parameterizedTypeReference);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", new Date());
        System.out.println(processFreemarker("<#setting locale=\"zh_CN\">${date?string('yyyy-MM-dd HH:mm:ss')}", map));
    }
}
