package com.bajins.demo;


import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class AnnotationPreDestroyTest {

    @PreDestroy
    public void destroy() {
        System.out.println("销毁：@PreDestory");
    }
}