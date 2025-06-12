package com.bajins.demo.soapws;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CxfSoapHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger LOGGER = Logger.getLogger(CxfSoapHandler.class.getName());

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        // 只处理响应消息
        Boolean isResponse = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (!isResponse) {
            return true;
        }
        try {
            // 获取SOAP消息
            SOAPMessage message = context.getMessage();
            // 在修改消息前添加
//            message.removeAllAttachments();
//            message.saveChanges(); // 必须保存修改

            SOAPPart soapPart = message.getSOAPPart();

//            NodeList nodes = message.getSOAPHeader().getElementsByTagNameNS("http://security.example.com", "SecurityToken");

            // 遍历元素并修改命名空间声明
            /*NodeList elements = soapPart.getElementsByTagName("*");
            for (int i = 0; i < elements.getLength(); i++) {
                Element elem = (Element) elements.item(i);
                if (elem.getNamespaceURI().equals("http://tempuri.org/")) {
                    elem.setAttribute("xmlns:my", "http://tempuri.org/"); // 添加新前缀
                }
            }*/
            NodeList elements = soapPart.getElementsByTagName("string");
            for (int i = 0; i < elements.getLength(); i++) {
                Element elem = (Element) elements.item(i);
                elem.setAttribute("xmlns", "http://tempuri.org/");
            }

            SOAPEnvelope envelope = soapPart.getEnvelope();

            // 移除默认命名空间（可选）
            envelope.removeNamespaceDeclaration("SOAP-ENV");
            // 添加自定义命名空间
//            envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
//            envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
//            envelope.addNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/");
            // 强制添加 xmlns:xsi
            envelope.addAttribute(new QName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xsi", XMLConstants.XMLNS_ATTRIBUTE), "http://www.w3.org/2001/XMLSchema-instance");
            // 强制添加 xmlns:xsd
            envelope.addAttribute(new QName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xsd", XMLConstants.XMLNS_ATTRIBUTE), "http://www.w3.org/2001/XMLSchema");
            // 添加 xmlns:soap (虽然JAX-WS通常不会缺少这个，以防万一)
            // 注意：SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE 是 SOAP 1.1 的命名空间
            // 如果是 SOAP 1.2，则应该是 SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE
            // 检查是否已是SOAP命名空间
            if (envelope.getNamespaceURI().equals(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
                // 实际上，这个通常不需要手动添加，因为envelope.getNamespaceURI()就是SOAP命名空间
                // 如果你想要强制前缀为'soap'并且不依赖默认，需要更复杂的逻辑，但通常JAX-WS会处理好
                // 这里的判断是为了确保SOAP命名空间不是以其他前缀存在的
                envelope.addNamespaceDeclaration("soap", SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE);
            }
            // 在 SOAP Header 添加元素
            /*SOAPHeader header = envelope.getHeader();
            if (header == null) {
                header = envelope.addHeader();
            }
            SOAPHeaderElement security = header.addHeaderElement(
                    new QName("http://security.example.com", "SecurityToken"));
            security.setTextContent("TOKEN-12345");*/

            SOAPBody body = envelope.getBody();

            // 遍历body中的所有子节点
            Iterator<?> bodyElements = body.getChildElements();
            while (bodyElements.hasNext()) {
                SOAPElement responseElement = (SOAPElement) bodyElements.next();

                // 查找并处理<return>节点
                processReturnNodes(responseElement);
            }

            // 强制更新SOAP消息
            message.saveChanges();
        } catch (SOAPException e) {
            LOGGER.log(Level.SEVERE, "处理SOAP消息时发生错误", e);
            return false;
            /*try {
                throw new SOAPFaultException(SOAPFactory.newInstance().createFault(
                        "认证失败", new QName("http://schemas.xmlsoap.org/soap/envelope/", "Client")));
            } catch (SOAPException ex) {
                throw new RuntimeException(ex);
            }*/
        }
        // 继续处理链
        return true;
    }

    @Override
    public Set<QName> getHeaders() {
        // 指定需要处理的SOAP头
        return Collections.emptySet();
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        // 错误处理逻辑
        return true;
    }

    @Override
    public void close(MessageContext context) {
        // 资源清理
    }

    /**
     * 处理SOAP元素中的<return>节点
     */
    private void processReturnNodes(SOAPElement parentElement) throws SOAPException {
        Iterator<?> childElements = parentElement.getChildElements();

        // 收集所有需要处理的<return>节点
        while (childElements.hasNext()) {
            Object child = childElements.next();
            if (child instanceof SOAPElement) {
                SOAPElement element = (SOAPElement) child;

                // 如果是<return>节点，将其子节点提升到父节点
                if ("return".equals(element.getLocalName())) {
                    // 保存<return>节点的所有子节点
                    Iterator<?> returnChildren = element.getChildElements();
                    while (returnChildren.hasNext()) {
                        SOAPElement returnChild = (SOAPElement) returnChildren.next();

                        // 将子节点添加到父节点
                        SOAPElement clonedChild = (SOAPElement) returnChild.cloneNode(true);
                        parentElement.appendChild(clonedChild);
                    }

                    // 移除<return>节点
                    parentElement.removeChild(element);
                }
            }
        }
    }
}
