package com.bajins.demo.soapws;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD) // 使用字段来映射XML元素
@XmlRootElement(name = "Web_StartCheck_V2", namespace = "http://tempuri.org/") // 对应SOAP请求体中的根元素名
@XmlType(propOrder = { // 定义XML元素的顺序
        "resourceName",
        "lotSN",
        "specialCode",
        "testSoft",
        "operator"
}, namespace = "http://tempuri.org/")
public class WebStartCheckV2 {

    @XmlElement(name = "ResourceName", required = true)
    protected String resourceName;
    @XmlElement(name = "LotSN", required = true)
    protected String lotSN;
    @XmlElement(name = "SpecialCode") // 可选，required默认为false
    protected String specialCode;
    @XmlElement(name = "TestSoft", required = true)
    protected String testSoft;
    @XmlElement(name = "Operator", required = true)
    protected String operator;

    // --- Getters and Setters ---
    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String value) {
        this.resourceName = value;
    }

    public String getLotSN() {
        return lotSN;
    }

    public void setLotSN(String value) {
        this.lotSN = value;
    }

    public String getSpecialCode() {
        return specialCode;
    }

    public void setSpecialCode(String value) {
        this.specialCode = value;
    }

    public String getTestSoft() {
        return testSoft;
    }

    public void setTestSoft(String value) {
        this.testSoft = value;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String value) {
        this.operator = value;
    }
}
