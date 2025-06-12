package com.bajins.demo.soapws;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.List;

@Import({CxfSoapService.class})
@Configuration
public class CxfSoapConfig implements WebMvcConfigurer {

    @Autowired(required = false)
    private Bus bus;
    @Autowired
    private CxfSoapService cxfSoapService;

    @Bean
    public Endpoint soapWebServiceEndpoint() {
        if (bus == null) {
            return null;
        } else {
            /*JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
            factory.setServiceClass(CxfSoapService.class);
            factory.setAddress("/CGweb/MES-WebService.asmx");
            factory.setServiceBean(new CxfSoapService());
            factory.create();*/
            EndpointImpl endpoint = new EndpointImpl(bus, cxfSoapService);
            // 配置Handler链
            List<Handler> handlerChain = new ArrayList<>();
            handlerChain.add(new CxfSoapHandler());
            endpoint.setHandlers(handlerChain);
            endpoint.publish("/CGweb/MES-WebService.asmx");
            return endpoint;
        }
    }
}