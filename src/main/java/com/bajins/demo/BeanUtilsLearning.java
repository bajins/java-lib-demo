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
