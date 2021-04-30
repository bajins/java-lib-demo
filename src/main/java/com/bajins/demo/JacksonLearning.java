package com.bajins.demo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JacksonLearning {

    private static void testJackJson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // 排除json字符串中实体类没有的字段 @JsonIgnore
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 序列化的时候序列对象的所有属性
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // 如果是空对象的时候,不抛异常
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 属性为null的转换 @JsonInclude(value = Include.NON_NULL)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 取消时间的转化格式,默认是时间戳,可以取消,同时需要设置要表现的时间格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 命名策略：使用属性名称，可以为不同字段设置不同的名称
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);// 驼峰命名，字段的首字母小写
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);// 驼峰命名，字段的首字母大写
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);// 字段小写，多个单词以下划线_分隔
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);// 字段小写，多个单词以中横线-分隔
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);// 字段小写，多个单词间无分隔符

        /*SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        // Person类的属性过滤器（只序列化car,house,name字段）
        filterProvider.addFilter("person", SimpleBeanPropertyFilter.filterOutAllExcept(Sets.newHashSet("car", "house", "name")));*/


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
