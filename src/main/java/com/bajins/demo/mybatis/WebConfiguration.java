package com.bajins.demo.mybatis;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> next : converters) {
            if (next instanceof Jaxb2RootElementHttpMessageConverter) {
                Jaxb2RootElementHttpMessageConverter jaxbConverter = (Jaxb2RootElementHttpMessageConverter) next;
                jaxbConverter.setProcessExternalEntities(false);
                jaxbConverter.setSupportDtd(true);
            }
        }
    }
}
