package com.bajins.demo.soapws;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.List;

@EnableWs // 启用Spring Web Services并生成WSDL
@Configuration
public class SpringSoapConfig extends WsConfigurerAdapter {

    /*@Bean
    public SaajSoapMessageFactory messageFactory() throws SOAPException {
        SaajSoapMessageFactory factory = new SaajSoapMessageFactory();
        // SOAPConstants.SOAP_1_1_PROTOCOL
        // 使用 SOAP 1.2
        factory.setSoapVersion(SoapVersion.SOAP_12);
        return factory;
    }*/

    @Bean
    public SpringSoapEndpointInterceptor springSoapEndpointInterceptor() {
        return new SpringSoapEndpointInterceptor();
    }

    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        // EndpointInterceptor
        // EndpointInterceptorAdapter
        // ClientInterceptor
        // 添加自定义 Handler
        interceptors.add(springSoapEndpointInterceptor());
    }

    // 如果需要配置 PayloadRootAnnotationMethodEndpointMapping 的其他属性，
    // 可以将其声明为一个Bean。在这种情况下，上面addInterceptors方法将不再管理这个特定bean。
    // @Bean
    // public PayloadRootAnnotationMethodEndpointMapping payloadRootAnnotationMethodEndpointMapping() {
    //     PayloadRootAnnotationMethodEndpointMapping mapping = new PayloadRootAnnotationMethodEndpointMapping();
    //     mapping.setInterceptors(new EndpointInterceptor[]{myEndpointInterceptor});
    //     mapping.setAlwaysHandleFaults(true); // 即使Endpoint找不到，也调用handleFault
    //     return mapping;
    // }

    // 首先，配置Spring WS的核心Servlet
    @Bean
    public ServletRegistrationBean messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        // 启用WSDL位置转换，这样WSDL中的service URL可以根据运行环境动态改变，动态生成WSDL的locationUri
        servlet.setTransformWsdlLocations(true);
        // 将SOAP请求映射到指定路径下
        // 关键点：将SOAP请求映射到 /CGweb/MES-WebService.asmx
        // 这样服务就能响应这个路径，包括 ?op=Web_StartCheck_V2 这样的查询参数（由Spring-WS内部处理）
        return new ServletRegistrationBean<>(servlet, "/CGweb/*"); // spring.webservices.path=/CGweb
    }

    // 第二步，配置WSDL的定义，使其可以通过 /xx/{serviceName}.wsdl 访问
    // 配置WSDL定义
    @Bean(name = "MES-WebService") // WSDL文件的名称将是 MES-WebService.wsdl
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema startCheckV2Schema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        // 服务名，WSDL中PortType的名称
        wsdl11Definition.setPortTypeName("MESWebServiceSoap");
        // 服务访问的URI，这决定了WSDL中SOAP endpoint的地址，与ServletRegistrationBean的路径匹配
        wsdl11Definition.setLocationUri("/CGweb/MES-WebService.asmx");
        // 目标命名空间，与SOAP请求/响应的命名空间一致
        wsdl11Definition.setTargetNamespace("http://tempuri.org/");
        // 引用请求和响应的XSD，Spring-WS会根据这些XSD来生成WSDL的类型部分
        // 注意：这里只处理了外部的请求和响应，因为内嵌的XML是作为字符串返回的
//        wsdl11Definition.setSchemaCollection(new XsdSchema[]{requestSchema, outerResponseSchema});
        wsdl11Definition.setSchema(startCheckV2Schema); // 引用我们定义的XSD Schema
        return wsdl11Definition;
    }

    // 第三步，定义XSD Schema。这里是根据我们前面定义的JAXB对象手动编写的XSD简化版
    // 实际项目中，可以通过Maven插件从JAXB对象生成XSD，或者使用已有的XSD文件
    @Bean
    public XsdSchema startCheckV2Schema() {
        // ClassPathResource 指向 resources 目录下的 XSD 文件
        return new SimpleXsdSchema(new ClassPathResource("spring-aop.xsd"));
    }

    // Bean for request XSD
    /*@Bean
    public XsdSchema requestSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/mes_request.xsd"));
    }

    // Bean for outer response XSD
    @Bean
    public XsdSchema outerResponseSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/mes_outer_response.xsd"));
    }

    // Bean for inner response XSD (虽然Spring-WS不会直接用它生成WSDL，但我们需要它来生成Java类并手动序列化)
    @Bean
    public XsdSchema innerResponseSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/mes_inner_response.xsd"));
    }*/
}