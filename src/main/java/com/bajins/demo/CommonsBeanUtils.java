package com.bajins.demo;


import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class CommonsBeanUtils {

    public static void main(String[] args) {
        try {
            // map转bean
            BeanUtils.populate("obj", new HashMap<>());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
