package com.bajins.demo;


import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class BeanUtilsLearning {

    /**
     * 获取所有非空的属性
     *
     * @param source
     * @return
     */
    public static String[] getPropertyNamesNotNull(Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = wrappedSource.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = wrappedSource.getPropertyValue(pd.getName());
            if (null != srcValue) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static String[] getPropertyNamesNotNullByStream(Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        // Java8 Stream获取非空属性
        String[] result = Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) != null)
                .toArray(String[]::new);
        return result;
    }

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
