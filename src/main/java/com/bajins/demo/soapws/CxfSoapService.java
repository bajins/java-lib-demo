package com.bajins.demo.soapws;


import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.BindingType;


@WebService(name = "MES-WebService", targetNamespace = "http://tempuri.org/")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@BindingType(value = javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public class CxfSoapService {

    @WebMethod(operationName = "Web_StartCheck_V2", action = "http://tempuri.org/Web_StartCheck_V2")
    @WebResult(name = "", targetNamespace = "http://tempuri.org")
    public WEBStartCheckV2Response webTestConnect(@WebParam(name = "LotSN", partName = "parameters", targetNamespace = "http://tempuri.org/") String lotSN,
                                                 @WebParam(name = "ResourceName", partName = "parameters", targetNamespace = "http://tempuri.org/") String resourceName,
                                                 @WebParam(name = "SpecialCode", partName = "parameters", targetNamespace = "http://tempuri.org/") String specialCode,
                                                 @WebParam(name = "Operator", partName = "parameters", targetNamespace = "http://tempuri.org/") String operator,
                                                 @WebParam(name = "TestSoft", partName = "parameters", targetNamespace = "http://tempuri.org/") String testSoft) {
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

}
