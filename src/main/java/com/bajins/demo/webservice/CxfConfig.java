package com.bajins.demo.webservice;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CxfConfig {

    @Autowired
    private ServerService serverService;

    /**
     * wsimport命令是jdk提供的，作用是根据使用说明书生成客户端代码，wsimport只支持SOAP1.1客户端的生成
     * wsimport -keep -extension http://localhost:8080/webService/ws/api?wsdl
     *
     * @return
     */
    @Bean
    public ServletRegistrationBean disServlet() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new CXFServlet(), "/webService" +
                "/*");
        return servletRegistrationBean;
    }

    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }

    @Bean
    public EndpointImpl endpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), serverService);
        endpoint.publish("/ws/api");
        System.out.println("服务发布成功！地址为：http://localhost:8080/webService/ws/api?wsdl");
        return endpoint;
    }
}
