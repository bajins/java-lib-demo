package com.bajins.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


/**
 * 在spring中使用aop注入时需注意：spring可能有RestTemplate的默认配置（请求头等），导致在某些情况下会有差异，所以最好是自己进行初始化
 */
public class RestTemplateLearning {

    public static void main(String[] args) throws JsonProcessingException {
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


        String url = "https://test.com/tags/{1}/test?page={2}&count={3}&order=new&before_timestamp=";
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class,
                "test", 1, 100);
    }
}
