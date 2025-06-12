package com.bajins.demo.soapws;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

@Endpoint // 标记这个类为一个Spring WS Endpoint
//@HandlerChain(file = "classpath:handler-chain.xml")
public class SpringSoapEndpoint {

    private static final String NAMESPACE_URI = "http://tempuri.org/";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "Web_StartCheck_V2") // 映射传入的SOAP请求
    @ResponsePayload // 标记方法的返回值作为SOAP响应体
    public WEBStartCheckV2Response handleStartCheckV2Request(@RequestPayload WebStartCheckV2 request) {
        System.out.println("Received StartCheckV2 Request:");
        System.out.println("  ResourceName: " + request.getResourceName());
        System.out.println("  LotSN: " + request.getLotSN());
        System.out.println("  SpecialCode: " + request.getSpecialCode());
        System.out.println("  TestSoft: " + request.getTestSoft());
        System.out.println("  Operator: " + request.getOperator());

        // --- 业务逻辑处理 ---
        // 假设这里进行产品SN、资源等校验，并根据结果生成内部XML
        // 实际应用中，这里会调用Service层或DAO层进行数据操作
        Root internalRootResponse = new Root();
        internalRootResponse.setTestResult("PASS"); // 假设测试结果是PASS
        internalRootResponse.setMac("146C27DF91C7"); // 假设MAC地址
        internalRootResponse.setiReturnMessage(
                "MES:产品条码[" + request.getLotSN() + "]检查通过，请开始测试90EF4AE354CE;;;;01;;A2DP测试_频率响应@100/119.43;" +
                        "A2DP测试_频率响应@1k/96.22;A2DP测试_频率响应@3.15k/97.08;A2DP测试_频率响应@50/121.22;" +
                        "A2DP测试_频率响应@500/101.05;A2DP测试_频率响应@5k/97.91;"
        );

        // 将内部XML对象转换为XML字符串
        String internalXmlString = marshalRootObjectToString(internalRootResponse);
        System.out.println("Internal XML String: " + internalXmlString);

        // 构建SOAP响应对象
        Root root = new Root();
        root.setiReturnMessage("获取成功");
        root.setTestResult("True");
        root.setMac("DDFSDA");

        WebWebServiceStartCheckV2string webWebServiceStartCheckV2string = new WebWebServiceStartCheckV2string();
        webWebServiceStartCheckV2string.setRoot(root);

        WebWebServiceStartCheckV2 webWebServiceStartCheckV2 = new WebWebServiceStartCheckV2();
        webWebServiceStartCheckV2.setWebWebServiceStartCheckV2string(webWebServiceStartCheckV2string);

        WEBStartCheckV2Response response = new WEBStartCheckV2Response();
        response.setWebWebServiceStartCheckV2(webWebServiceStartCheckV2);

        return response;
    }

    /**
     * 将Root对象转换为XML字符串的方法
     */
    private String marshalRootObjectToString(Root root) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Root.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // 格式化输出，使其更易读 (可选)
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // 让marshaller不生成XML声明 (<?xml version="1.0" encoding="utf-8"?>)
            // 因为示例响应中内部XML字符串包含此声明，所以此处保持默认行为或手动去除
            // 如果内部XML字符串不应包含声明，可以设置：
            // jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); // 不生成XML声明

            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(root, sw);
            return sw.toString();
        } catch (JAXBException e) {
            System.err.println("Error marshalling Root object to XML string: " + e.getMessage());
            // 生产环境中应有更完善的异常处理
            return ""; // 返回空字符串或错误信息
        }
    }


    /*private static final String NAMESPACE_URI = "http://tempuri.org/";
    private static final String INNER_XML_NAMESPACE_URI = "http://www.example.com/MESWebService"; // 内部XML的命名空间

    private JAXBContext innerXmlJAXBContext;

    @Autowired
    public WebServiceEndpoint() throws JAXBException {
        // 创建JAXBContext用于内部XML (Root对象) 的序列化
        // 注意这里是 com.example.meswebservice.schema.Root.class，而不是 ObjectFactory
        // 如果你的Root类生成在其他包下或者有多个Root类，请确保指向正确
        this.innerXmlJAXBContext = JAXBContext.newInstance(Root.class);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "Web_StartCheck_V2")
    @ResponsePayload
    public WEBStartCheckV2Response handleWebStartCheckV2(@RequestPayload WebStartCheckV2 request) {
        log.info("Received Web_StartCheck_V2 request:");
        log.info("  ResourceName: {}", request.getResourceName());
        log.info("  LotSN: {}", request.getLotSN());
        log.info("  SpecialCode: {}", request.getSpecialCode());
        log.info("  TestSoft: {}", request.getTestSoft());
        log.info("  Operator: {}", request.getOperator());

        // 1. 业务逻辑处理（此处为示例模拟）
        String returnMessage;
        String testResult;
        String macAddress;

        // 示例：根据LotSN简单判断
        if (request.getLotSN() != null && request.getLotSN().startsWith("CB54")) {
            returnMessage = "MES:产品条码[" + request.getLotSN() + "]检查通过，请开始测试" + generateRandomHex(12) + ";;;;01;;A2DP测试_频率响应@100/119.43;A2DP测试_频率响应@1k/96.22;A2DP测试_频率响应@3.15k/97.08;A2DP测试_频率响应@50/121.22;A2DP测试_频率响应@500/101.05;A2DP测试_频率响应@5k/97.91;";
            testResult = "PASS";
            macAddress = generateRandomHex(12).toUpperCase(); // 假设生成一个MAC地址
        } else {
            returnMessage = "MES:产品条码[" + request.getLotSN() + "]检查失败，产品类型不匹配或不存在！";
            testResult = "FAIL";
            macAddress = ""; // 失败时不返回MAC
        }

        // 2. 构建内部XML (Root对象)
        Root innerRoot = new Root();
        innerRoot.setiReturnMessage(returnMessage);
        innerRoot.setTestResult(testResult);
        innerRoot.setMac(macAddress);

        // 3. 将内部XML对象序列化为XML字符串
        String innerXmlString = "";
        try {
            Marshaller marshaller = innerXmlJAXBContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // 格式化输出
            // 不输出XML声明，因为它是作为内容嵌入的
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

            StringWriter sw = new StringWriter();
            marshaller.marshal(innerRoot, sw);
            innerXmlString = sw.toString();
            log.info("Generated inner XML string:\n{}", innerXmlString);
        } catch (JAXBException e) {
            log.error("Error marshalling inner XML (Root object): {}", e.getMessage(), e);
            // 错误处理：如果内部XML序列化失败，可以返回一个错误消息
            innerXmlString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root><I_ReturnMessage>Internal Server Error: Failed to generate response XML.</I_ReturnMessage><TestResult>FAIL</TestResult><Mac></Mac></Root>";
        }

        // 4. 构建外部SOAP响应
        WEBStartCheckV2Response response = new WEBStartCheckV2Response();
        WebStartCheckV2ResponseType outerResponse = new WebStartCheckV2ResponseType();
        outerResponse.setString(innerXmlString); // 设置内部XML字符串
        response.setWEBWebStartCheckV2(outerResponse); // 注意这里的命名，是JAXB生成的

        log.info("Sending Web_StartCheck_V2 response.");
        return response;
    }

    // 辅助方法：生成随机十六进制字符串
    private String generateRandomHex(int length) {
        StringBuilder sb = new StringBuilder();
        String hexChars = "0123456789ABCDEF";
        for (int i = 0; i < length; i++) {
            sb.append(hexChars.charAt((int) (Math.random() * hexChars.length())));
        }
        return sb.toString();
    }*/
}
