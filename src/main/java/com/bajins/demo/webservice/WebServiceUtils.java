package com.bajins.demo.webservice;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.json.XML;
import org.springframework.util.Assert;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * 天气预报 http://www.webxml.com.cn/WebServices/WeatherWebService.asmx?WSDL
 * http://www.webxml.com.cn/WebServices/IpAddressSearchWebService.asmx?wsdl
 */
public class WebServiceUtils {

    /**
     * 请求WebService
     *
     * @param url          地址
     * @param params       参数
     * @param namespaceURI 命名空间（接口类包名）
     * @param localPart    方法名
     * @return
     * @throws Exception
     */
    public static Object[] sendPostForWebService(String url, String params, String namespaceURI, String localPart)
            throws Exception {
        Assert.hasText(url, "URL不能为空");
        Assert.hasText(params, "参数不能为空");
        Assert.hasText(namespaceURI, "命名空间不能为空");
        Assert.hasText(localPart, "方法名不能为空");

        // 创建 JaxWsDynamicClientFactory 工厂实例
        JaxWsDynamicClientFactory factory = JaxWsDynamicClientFactory.newInstance();
        // 创建客户端
        Client client = factory.createClient(url);
        // 根据指定的命名空间（接口类包名）、方法名新建QName对象
        QName qname = new QName(namespaceURI, localPart);
        return client.invoke(qname, params);
    }

    public static <T> T getProxyService(String wsdl, Class<T> serviceClass) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(serviceClass);
        factory.setAddress(wsdl);

        T service = (T) factory.create();

        Client client = ClientProxy.getClient(service);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setConnectionTimeout(10 * 1000);
        policy.setReceiveTimeout(60 * 1000);
        conduit.setClient(policy);
        return service;
    }

    public void getSoap(String url, String targetNamespace, String pName, String method, String[] argsName,
                        String[] args) throws SOAPException {

        //WSDL中定义的端口的QName
        QName portName = new QName(targetNamespace, pName);

        //创建动态Service实例
        Service service = Service.create(new QName(targetNamespace, method));
        service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, url);

        //创建一个dispatch实例
        Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);

        // Use Dispatch as BindingProvider
        BindingProvider bp = (BindingProvider) dispatch;

        // 配置RequestContext以发送SOAPAction HTTP标头
        Map<String, Object> rc = dispatch.getRequestContext();
        rc.put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        rc.put(BindingProvider.SOAPACTION_URI_PROPERTY, targetNamespace + method);

        // 获取预配置的SAAJ MessageFactory
        MessageFactory factory = ((SOAPBinding) bp.getBinding()).getMessageFactory();

        // 创建SOAPMessage请求
        SOAPMessage request = factory.createMessage();
        // 请求体
        SOAPBody body = request.getSOAPBody();

        // Compose the soap:Body payload
        QName payloadName = new QName(targetNamespace, method);

        SOAPBodyElement payload = body.addBodyElement(payloadName);
        if (args.length > 0) {
            for (int i = 0; i < argsName.length; i++) {
                payload.addChildElement(argsName[i]).setValue(args[i]);
            }
        }
        /*payload.addChildElement("startCity").setValue("北京");
        payload.addChildElement("lastCity").setValue("上海");
        payload.addChildElement("theDate").setValue("2019-06-07");
        payload.addChildElement("userID").setValue("");

        SOAPElement message = payload.addChildElement(INPUT_NMAE);
        message.addTextNode("88888");*/

        //调用端点操作并读取响应
        SOAPMessage reply = dispatch.invoke(request);
        //reply.writeTo(System.out);

        // 处理响应结果
        Document doc = reply.getSOAPPart().getEnvelope().getBody().extractContentAsDocument();

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
            /*DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = docFactory.newDocumentBuilder();
            builder.setEntityResolver((publicId, systemId) -> {
                // return new InputSource(new ByteArrayInputStream("".getBytes()));
                return new InputSource(new StringReader(""));
            });
            Document document = builder.parse(file);
            document.setXmlStandalone(false);
            // document.setXmlVersion("1.0");*/

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("encoding", "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // 换行
            //transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//mybatis.org//DTD Config 3.0//EN");
            // 把xml中的dtd指向到缓存目录
            //transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "");
            transformer.transform(new DOMSource(doc), new StreamResult(bos));

            System.out.println(XML.toJSONObject(bos.toString()));
        } catch (TransformerException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        StringBuffer soapRequestData = new StringBuffer("""
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
            <soap:Body>
                <GetData xmlns="http://localhost/"
            </soap:Body>
        </soap:Envelope>
        """);
    }
}
