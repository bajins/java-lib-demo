package com.bajins.demo.soapws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "WEB_StartCheck_V2Response") // 对应SOAP响应体中的根元素名
public class WEBStartCheckV2Response {

    @XmlElement(name = "WEB_Web_StartCheck_V2", required = true)
    protected WebWebServiceStartCheckV2 webWebServiceStartCheckV2; // 这个字段将存放内部的XML字符串

    // --- Getters and Setters ---
    public WebWebServiceStartCheckV2 getWebWebServiceStartCheckV2() {
        return webWebServiceStartCheckV2;
    }

    public void setWebWebServiceStartCheckV2(WebWebServiceStartCheckV2 value) {
        this.webWebServiceStartCheckV2 = value;
    }
}


