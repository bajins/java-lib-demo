package com.bajins.demo.soapws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "WEB_Web_StartCheck_V2") // 对应SOAP响应体中的根元素名
public class WebWebServiceStartCheckV2 {

    @XmlElement(name = "string", required = true)
    protected WebWebServiceStartCheckV2string webWebServiceStartCheckV2string; // 这个字段将存放内部的XML字符串

    // --- Getters and Setters ---

    public WebWebServiceStartCheckV2string getWebWebServiceStartCheckV2string() {
        return webWebServiceStartCheckV2string;
    }

    public void setWebWebServiceStartCheckV2string(WebWebServiceStartCheckV2string webWebServiceStartCheckV2string) {
        this.webWebServiceStartCheckV2string = webWebServiceStartCheckV2string;
    }
}

