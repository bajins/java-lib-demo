package com.bajins.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JacksonLearning {

    private static void testJackJson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // 排除json字符串中实体类没有的字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
