package com.bajins.demo;

import com.alibaba.fastjson.JSON;
import com.expediagroup.beans.BeanUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import com.github.jsonzou.jmockdata.JMockData;
import com.rits.cloning.Cloner;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import io.github.sugarcubes.cloner.Cloners;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.modelmapper.ModelMapper;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.util.SerializationUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 复杂的深拷贝
 *
 * @author bajins
 */
public class DeepCopy {
    // private static final XStream xstream = new XStream();
    private static final ThreadLocal<XStream> threadLocalXStream = ThreadLocal.withInitial(() -> {
        XStream xs = new XStream();
        xs.addPermission(AnyTypePermission.ANY);
        return xs;
    });

    static {
        // xstream.addPermission(AnyTypePermission.ANY);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        EditDataResult<UserForm> result = new EditDataResult<>();
        result.setData(JMockData.mock(UserForm.class));
        Map<String, Object> extend = result.getExtend();
        extend.put("u1", JMockData.mock(UserForm.class));
        extend.put("u2", JMockData.mock(UserForm.class));
        extend.put("ulist", Collections.singletonList(JMockData.mock(UserForm.class)));
        result.setExtend(extend);
        System.out.println(JSON.toJSONString(result));

        /*
         jackson 序列化为json 再反序列化
         */
        EditDataResult<UserForm> deepCopy;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<EditDataResult<UserForm>> typeReference = new TypeReference<>() {
            };
            deepCopy = objectMapper
                    .readValue(objectMapper.writeValueAsString(result), typeReference);
            System.out.println(JSON.toJSONString(deepCopy));
            System.out.println(deepCopy.getData() == result.getData());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        /*
         xstream 序列化为xml 再反序列化
         */
        XStream xstream = threadLocalXStream.get();
        xstream.allowTypesByWildcard(new String[]{"**"});
        // 禁用引用
        xstream.setMode(XStream.NO_REFERENCES);
        // 允许任何类型
        xstream.addPermission(AnyTypePermission.ANY);
        deepCopy = (EditDataResult<UserForm>) xstream
                .fromXML(xstream.toXML(result));
        System.out.println(JSON.toJSONString(deepCopy));
        System.out.println(deepCopy.getData() == result.getData());

        /*
         cloner 直接深拷贝
         */
        Cloner cloner = new Cloner();
        deepCopy = cloner.deepClone(result);
        System.out.println(JSON.toJSONString(deepCopy));
        System.out.println(deepCopy.getData() == result.getData());

        /*
         使用sugar-cubes-cloner直接深拷贝
         */
        deepCopy = Cloners.reflection().clone(result);
        System.out.println(JSON.toJSONString(deepCopy));
        System.out.println(deepCopy.getData() == result.getData());

        /*
         orika 直接深拷贝，经测试，对泛型类拷贝失败，是空对象
         */
        deepCopy = new DefaultMapperFactory.Builder().build().getMapperFacade().map(result, EditDataResult.class);
        System.out.println(JSON.toJSONString(deepCopy));
        System.out.println(deepCopy.getData() == result.getData());

        /*
         使用Dozer直接深拷贝，经测试，对泛型类拷贝失败，是空对象
         */
        deepCopy = DozerBeanMapperBuilder.buildDefault().map(result, EditDataResult.class);
        System.out.println(JSON.toJSONString(deepCopy));
        System.out.println(deepCopy.getData() == result.getData());

        /*
         使用ModelMapper直接深拷贝，经测试，为浅拷贝
         */
        deepCopy = new ModelMapper().map(result, EditDataResult.class);
        System.out.println(JSON.toJSONString(deepCopy));
        System.out.println(deepCopy.getData() == result.getData());

        /*
         使用bull直接深拷贝，经测试，为浅拷贝
         */
        BeanUtils beanUtils = new BeanUtils();
        deepCopy = beanUtils.getTransformer().transform(result, EditDataResult.class);
        System.out.println(JSON.toJSONString(deepCopy));
        System.out.println(deepCopy.getData() == result.getData());


        /*
        使用org.springframework.util序列化并反序列化，必须实现Serializable接口
         */
        // EditDataResult<UserForm> deserialize = (EditDataResult<UserForm>) SerializationUtils.clone(serialize);
        /*
        使用apache.commons.lang3序列化并反序列化，必须实现Serializable接口
         */
        // EditDataResult<UserForm> deserialize = (EditDataResult<UserForm>) org.apache.commons.lang3.SerializationUtils.clone(result)
    }


    public static class EditDataResult<T> extends EditResult {
        private T data;

        public T getData() {
            return this.data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    public static class EditResult {
        private String resultCode = "0000";
        private String resultMsg;
        private int count;
        private Map<String, Object> extend = new HashMap<>();

        public String getResultCode() {
            return this.resultCode;
        }

        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public String getResultMsg() {
            return this.resultMsg;
        }

        public void setResultMsg(String resultMsg) {
            this.resultMsg = resultMsg;
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public Map<String, Object> getExtend() {
            return this.extend;
        }

        public void setExtend(Map<String, Object> extend) {
            this.extend = extend;
        }

        public void put(String key, Object value) {
            this.extend.put(key, value);
        }

        public void remove(String key) {
            this.extend.remove(key);
        }
    }

    public static class UserForm {
        private String name;
        private Integer age;
        private String sex;
        private String birthday;
        private String address;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}
