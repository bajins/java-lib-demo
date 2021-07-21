package com.bajins.demo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JacksonLearning {

    private static void testJackJson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // 排除json字符串中实体类没有的字段 @JsonIgnore
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许出现特殊字符和转义符
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true); // 旧版本
        //objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        // 序列化的时候序列对象的所有属性
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // 如果是空对象的时候,不抛异常
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 属性为null的转换 @JsonInclude(value = Include.NON_NULL)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 取消时间的转化格式,默认是时间戳,可以取消,同时需要设置要表现的时间格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //GMT+8
        //map.put("CTT", "Asia/Shanghai");
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 日期格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // https://zhuanlan.zhihu.com/p/277834439
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        objectMapper.configure(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, false); // true忽略已被注册的 module
        objectMapper.registerModule(javaTimeModule); // 避免无效，先把IGNORE_DUPLICATE_MODULE_REGISTRATIONS设为false
        objectMapper.configure(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, true);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        // Include.NON_NULL 属性为NULL 不序列化
        // ALWAYS // 默认策略，任何情况都执行序列化
        // NON_EMPTY // null、集合数组等没有内容、空字符串等，都不会被序列化
        // NON_DEFAULT // 如果字段是默认值，就不会被序列化
        // NON_ABSENT // null的不会序列化，但如果类型是AtomicReference，依然会被序列化
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 允许字段名没有引号（可以进一步减小json体积）：
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 允许单引号：
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 允许C和C++样式注释：
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // 序列化结果格式化，美化输出
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 枚举输出成字符串
        // WRITE_ENUMS_USING_INDEX：输出索引
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        //空对象不要抛出异常：
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        //Date、Calendar等序列化为时间格式的字符串(如果不执行以下设置，就会序列化成时间戳格式)：
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        //反序列化时，遇到未知属性不要抛出异常：
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //反序列化时，遇到忽略属性不要抛出异常：
        objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        //反序列化时，空字符串对于的实例属性为null：
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        // 命名策略：使用属性名称，可以为不同字段设置不同的名称
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);// 驼峰命名，字段的首字母小写
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);// 驼峰命名，字段的首字母大写
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);// 字段小写，多个单词以下划线_分隔
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);// 字段小写，多个单词以中横线-分隔
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);// 字段小写，多个单词间无分隔符

        /*SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        // Person类的属性过滤器（只序列化car,house,name字段）
        filterProvider.addFilter("person", SimpleBeanPropertyFilter.filterOutAllExcept(Sets.newHashSet("car",
        "house", "name")));*/


        // HashMap<String,String> 转json
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", "jack");
        map.put("city", "beijin");
        String mjson = objectMapper.writeValueAsString(map);
        System.out.println(mjson);

        // json转 HashMap<String,String>
        HashMap<String, String> mmap = objectMapper.readValue(mjson, HashMap.class);
        System.out.println(mmap);

        // json转 Map<String, Object>
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(HashMap.class, String.class,
                Object.class);
        JavaType javaType1 = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class,
                Object.class);
        Map<String, Object> map1 = objectMapper.readValue(mjson, javaType);
        System.out.println(map1);

        // 转换为 Map<String, Object>
        Map<String, Object> map2 = objectMapper.readValue(mjson, new TypeReference<HashMap<String, Object>>() {
        });
        System.out.println(map2);


        // List<Map<String, String>> 转json
        List<Map<String, String>> list = new ArrayList<>();
        list.add(map);
        String ljson = objectMapper.writeValueAsString(list);
        System.out.println(ljson);

        // json转 转换为 ArrayList<Map<String,Object>>
        ArrayList<Map<String, Object>> lm = objectMapper.readValue(ljson, ArrayList.class);
        System.out.println(lm);

        // 转换为 ArrayList<Map<String,Object>>
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class,
                HashMap.class);
        CollectionType collectionType1 = objectMapper.getTypeFactory().constructCollectionType(List.class,
                javaType);
        ArrayList<HashMap<String, Object>> list1 = objectMapper.readValue(ljson, collectionType);
        System.out.println(list1);

        // 转换为 ArrayList<Map<String,Object>>
        TypeReference<ArrayList<HashMap<String, Object>>> typeReference = new TypeReference<ArrayList<HashMap<String,
                Object>>>() {
        };
        ArrayList<Map<String, Object>> list2 = objectMapper.readValue(ljson, typeReference);
        System.out.println(list2);
    }


    public static void main(String[] args) throws IOException {
        testJackJson();
    }
}


/**
 * 自定义命名策略，为字段添加下划线前缀，其中setter方法使用默认的字段名
 */
/*
public static class AppendPrefixStrategyForSetter extends PropertyNamingStrategy.PropertyNamingStrategyBase {
    @Override
    public String translate(String input) {
        return '_' + input;
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return defaultName;
    }
}*/


/**
 * 出参保留两位小数
 *
 * @JsonDeserialize(using = DeserializerBigDecimal.class)
 */
class DeserializerBigDecimal extends JsonDeserializer<BigDecimal> {
    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String value = jsonParser.getValueAsString();
        if (!StringUtils.hasText(value)) {
            return null;
        }
        // 这里取floor
        return new BigDecimal(value).setScale(2, RoundingMode.FLOOR);
    }
}


/**
 * 入参保留两位小数
 *
 * @JsonSerialize(using = SerializerBigDecimal.class)
 */
class SerializerBigDecimal extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal bigDecimal, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        // 注意这里不能使用jsonGenerator.writeNumber(result);方法，不然会把.00去掉
        jsonGenerator.writeString(bigDecimal.setScale(2, RoundingMode.FLOOR).toPlainString());
    }
}