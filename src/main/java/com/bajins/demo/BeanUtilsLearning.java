package com.bajins.demo;


import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class BeanUtilsLearning {

    public static void main(String[] args) {
        try {
            // map转bean
            org.apache.commons.beanutils.BeanUtils.populate("obj", new HashMap<>());

            /**
             * 对象拷贝
             */
            // No value specified for 'Date'：复制对象内存在Date类型的属性，但是Date没有初始值，因此需要提前设置初始值
            //ConvertUtils.register(new DateConverter(null), java.util.Date.class);
            //org.apache.commons.beanutils.BeanUtils.copyProperties();

            //org.apache.commons.beanutils.PropertyUtils.copyProperties();
            //org.springframework.beans.BeanUtils.copyProperties();
            //org.springframework.cglib.beans.BeanCopier.create().copy();

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
