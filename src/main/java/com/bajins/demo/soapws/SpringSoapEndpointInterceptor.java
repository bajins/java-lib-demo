package com.bajins.demo.soapws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

public class SpringSoapEndpointInterceptor extends EndpointInterceptorAdapter {

    private static final Logger log = LoggerFactory.getLogger(SpringSoapEndpointInterceptor.class);

    // 1. handleRequest: 在Endpoint处理请求之前调用
    // 返回true表示继续处理请求，返回false表示中断请求处理，拦截器链和Endpoint都不会被调用。
    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        log.info("MyEndpointInterceptor: handleRequest method called.");
        log.info("Incoming request SOAP message body:\n{}", messageContext.getRequest().getPayloadSource());

        // 可以在这里进行认证、授权或日志记录
        // 例如，检查特定的SOAP Header
        // if (!authorize(messageContext)) {
        //     log.warn("Authorization failed for incoming request.");
        //     return false; // 中断请求处理
        // }
        // 继续处理请求
        return true;
    }

    // 2. handleResponse: 在Endpoint成功处理请求并生成响应之后调用
    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        log.info("MyEndpointInterceptor: handleResponse method called.");
        log.info("Outgoing response SOAP message body:\n{}", messageContext.getResponse().getPayloadSource());
        // 可以在这里修改响应、添加Header或记录响应信息

        // 1. 获取 SOAP 响应消息
        /*SoapMessage soapResponse = (SoapMessage) messageContext.getResponse();
        SoapBody body = soapResponse.getSoapBody();
        // 2. 获取响应负载的 Source (XML 内容)
        Source payloadSource = body.getPayloadSource();*/

        SaajSoapMessage saajMessage = (SaajSoapMessage) messageContext.getResponse();
        // 获取SOAP消息
        SOAPMessage message = saajMessage.getSaajMessage();

        SOAPPart soapPart = message.getSOAPPart();
        NodeList elements = soapPart.getElementsByTagName("string");
        for (int i = 0; i < elements.getLength(); i++) {
            Element elem = (Element) elements.item(i);
            elem.setAttribute("xmlns", "http://tempuri.org/");
        }

        SOAPEnvelope envelope = soapPart.getEnvelope();

        // 移除默认命名空间（可选）
        envelope.removeNamespaceDeclaration("SOAP-ENV");
        // 添加自定义命名空间
        envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        envelope.addNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/");

        // 提取 SOAP Body 的内容
//        SOAPBody soapBody = message.getSOAPBody();
//        NodeList nodeList = soapBody.getChildNodes();

        // 强制更新SOAP消息
        message.saveChanges();

        // 返回 true 表示继续处理拦截器链，false 表示停止
        return true;
    }

    // 3. handleFault: 在Endpoint抛出异常但未成功完成请求处理时调用
    @Override
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        log.warn("MyEndpointInterceptor: handleFault method called. An error occurred.");
        log.warn("Fault SOAP message body:\n{}", messageContext.getResponse().getPayloadSource());
        // 可以在这里记录错误、转换错误消息或发送通知
        return true;
    }

    // 4. afterCompletion: 在完全处理完请求（包括渲染视图或发送响应）后调用，无论是否抛出异常。
    // 适用于资源清理等操作。
    // exception参数如果为null，表示没有异常发生；否则表示抛出的异常。
    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
        log.info("MyEndpointInterceptor: afterCompletion method called.");
        if (ex != null) {
            log.error("Request completed with exception: {}", ex.getMessage());
        } else {
            log.info("Request completed successfully.");
        }
        // 可以在这里进行资源清理、性能统计等
    }
}
