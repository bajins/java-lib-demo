package com.bajins.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * https://github.com/alibaba/fastjson
 * https://github.com/alibaba/fastjson2
 */
public class FastJsonLearning {

    /**
     * https://binarylife.icu/articles/1032
     * https://www.cnblogs.com/MattCheng/p/8621707.html
     * https://www.cnblogs.com/diegodu/p/5950057.html
     * https://blog.csdn.net/weixin_72984629/article/details/126852879
     * <p>
     * Feature.DisableFieldSmartMatch 用于关闭下划线、大小写 反序列化
     * PropertyNamingStrategy https://github.com/alibaba/fastjson/wiki/PropertyNamingStrategy_cn
     * SerializeFilterable 过滤器列表
     * AfterFilter
     * BeforeFilter
     * ContextValueFilter
     * LabelFilter
     * NameFilter
     * PascalNameFilter 序列化首字符大写
     * PropertyFilter
     * PropertyPreFilter
     * SerializeFilter 序列化过滤器
     * SimplePropertyPreFilter
     * ValueFilter
     * <p>
     * SerializerFeature 序列化属性 https://blog.csdn.net/qq_45441466/article/details/110393204
     * SerializeConfig 序列化时的配置
     * ObjectSerializer
     * ObjectDeserializer
     *
     * @param args
     */
    public static void main(String[] args) {
        /*
        JSONObject.toJSONString(request, new PropertyPreFilter() { // 序列化HttpServletRequest
            @Override
            public boolean apply(JSONSerializer paramJSONSerializer, Object paramObject, String paramString) {
                List<String> names = new ArrayList<>();
                names.add("asyncContext");
                names.add("asyncStarted");
                names.add("parts");
                names.add("reader");
                return !names.contains(paramString);
            }
        });
        // 定义一个转换消息的对象
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        List<MediaType> fastMediaTypes = new ArrayList<MediaType>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastConverter.setSupportedMediaTypes(fastMediaTypes);
        // 添加fastjson的配置信息 比如 ：是否要格式化返回的json数据
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        // WriteMapNullValue把空的值的key也返回  需要其他的序列化规则按照格式设置即可
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteMapNullValue);
        // 处理中文乱码问题
        fastJsonConfig.setCharset(StandardCharsets.UTF_8);
        // 在转换器中添加配置信息
        fastConverter.setFastJsonConfig(fastJsonConfig);
        
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.configNullAsEmpty(true); // 将null转换为空字符串输出
        */
        String json = "{\"user_Name\":\"t\",\"age\":18}";

        ParserConfig parserConfig = new ParserConfig();
        //parserConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase; // Map、JSONObject、toJavaObject无效
        // 关闭智能匹配
        //Test test = JSON.parseObject(str, Test.class, parserConfig, Feature.DisableFieldSmartMatch);
        //test = object.toJavaObject(Test.class); // 智能匹配会失效
        JSONObject object = (JSONObject) JSON.parse(json, parserConfig); // 智能匹配会失效
        System.out.println(object);

        // 反序列化：MAP下划线转驼峰
        parserConfig.putDeserializer(Map.class, new PascalNameDeserializer());
        parserConfig.putDeserializer(JSONObject.class, new PascalNameDeserializer());
        object = JSON.parseObject(json, JSONObject.class, parserConfig); // parse方法无效
        System.out.println(object);

        // 序列化：驼峰转下划线
        SerializeConfig serializeConfig = new SerializeConfig();
        //serializeConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase; // Map、JSONObject、toJavaObject无效
        //serializeConfig.put(Map.class, new PascalNameSerializer());
        //serializeConfig.put(JSONObject.class, new PascalNameSerializer());
        serializeConfig.addFilter(Map.class, new UnderLineToCamelCaseNameFilter());
        serializeConfig.addFilter(JSONObject.class, new UnderLineToCamelCaseNameFilter());
        json = JSON.toJSONString(object, serializeConfig, new ValueFilter() {
            @Override
            public Object process(Object object, String key, Object value) {
                return Objects.requireNonNullElse(value, "");
            }
        });
        // 不启用jsonpath来解决循环依赖引用$ref
        //json = JSON.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
        System.out.println(json);

        // 深拷贝
        // 生成包含@type的JSON，便于反序列化时精准还原类型
        String json1 = JSON.toJSONString(object, serializeConfig, SerializerFeature.WriteClassName);
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        ParserConfig parserConfig1 = new ParserConfig();
        parserConfig1.setAutoTypeSupport(true);
        // ParserConfig.getGlobalInstance().setAutoTypeSupport(true); // 全局显式启用 AutoType （不推荐生产环境）
        // parserConfig1.addAccept("com.bajins.demo");
        Map<String, Object> o = JSON.parseObject(json1, typeReference.getType(), parserConfig1);
    }

    /**
     * 下划线转驼峰
     */
    public static class CamelCaseNameFilter implements NameFilter {
        @Override
        public String process(Object object, String name, Object value) {
            if (name.contains("_")) {
                StringBuilder sb = new StringBuilder();
                String[] words = name.split("_");
                for (int i = 0; i < words.length; i++) {
                    if (i == 0) {
                        sb.append(words[i]);
                    } else {
                        sb.append(StringUtils.capitalize(words[i]));
                    }
                }
                return sb.toString();
            }
            return name;
        }
    }

    /**
     * 驼峰转下划线
     */
    public static class UnderLineToCamelCaseNameFilter implements NameFilter {
        @Override
        public String process(Object object, String name, Object value) {
            if (name.contains("_")) { // 如果有下划线则不进行驼峰转下划线
                return name;
            }
            Pattern p = Pattern.compile("[A-Z]");
            StringBuilder builder = new StringBuilder(name);
            Matcher mc = p.matcher(name);
            int i = 0;
            while (mc.find()) {
                builder.replace(mc.start() + i, mc.end() + i, "_" + mc.group().toLowerCase());
                i++;
            }
            if ('_' == builder.charAt(0)) {
                builder.deleteCharAt(0);
            }
            return builder.toString();
        }
    }


    /**
     * 将 Map 中的下划线转驼峰后再序列化
     */
    public static class PascalNameSerializer implements ObjectSerializer {
        @Override
        public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
            if (!(object instanceof Map)) {
                return;
            }
            Map<String, Object> map = (Map<String, Object>) object;
            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key.contains("_")) {
                    StringBuilder sb = new StringBuilder();
                    boolean upperCase = false;
                    for (int i = 0; i < key.length(); i++) {
                        char c = key.charAt(i);
                        if (c == '_') {
                            upperCase = true;
                        } else {
                            if (upperCase) {
                                sb.append(Character.toUpperCase(c));
                                upperCase = false;
                            } else {
                                sb.append(Character.toLowerCase(c));
                            }
                        }
                    }
                    key = sb.toString();
                }
                result.put(key, value);
            }
            serializer.write(result);
        }
    }

    /**
     * 将 Map 中的下划线转驼峰后再反序列化
     */
    public static class PascalNameDeserializer implements ObjectDeserializer {
        @Override
        public Object deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            /*if (!type.getTypeName().equals("Map")) {
                return parser.parse(type);
            }*/
            JSONObject jsonObject = new JSONObject();
            Map<String, Object> parse = (Map<String, Object>) parser.parse(type);
            for (Map.Entry<String, Object> entry : parse.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key.contains("_")) {
                    StringBuilder sb = new StringBuilder();
                    boolean upperCase = false;
                    for (int i = 0; i < key.length(); i++) {
                        char c = key.charAt(i);
                        if (c == '_') {
                            upperCase = true;
                        } else {
                            if (upperCase) {
                                sb.append(Character.toUpperCase(c));
                                upperCase = false;
                            } else {
                                sb.append(Character.toLowerCase(c));
                            }
                        }
                    }
                    key = sb.toString();
                }
                jsonObject.put(key, value);
            }
            return jsonObject;
        }

        @Override
        public int getFastMatchToken() {
            return JSONToken.LBRACE;
        }
    }


    /**
     * JSONArray/JSONObject 下划线转驼峰
     *
     * @param json JSONArray/JSONObject
     */
    public static void convertNaming(Object json) {
        if (json instanceof JSONArray arr) {
            for (Object obj : arr) {
                convertNaming(obj);
            }
        } else if (json instanceof JSONObject jo) {
            Set<String> keys = jo.keySet();
            String[] array = keys.toArray(new String[0]);
            for (String key : array) {
                Object value = jo.get(key);
                String[] key_strs = key.split("_");
                if (key_strs.length > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < key_strs.length; i++) {
                        String ks = key_strs[i];
                        if (StringUtils.isBlank(ks)) {
                            continue;
                        }
                        if (i == 0) {
                            sb.append(ks);
                        } else {
                            int c = ks.charAt(0);
                            if (c >= 97 && c <= 122) {
                                int v = c - 32;
                                sb.append((char) v);
                                if (ks.length() > 1) {
                                    sb.append(ks.substring(1));
                                }
                            } else {
                                sb.append(ks);
                            }
                        }
                    }
                    jo.remove(key);
                    jo.put(sb.toString(), value);
                }
                convertNaming(value);
            }
        }
    }

}
