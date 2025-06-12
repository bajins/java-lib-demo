package com.bajins.demo.soapws;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Root") // 根元素名为Root
@XmlType(propOrder = {
        "iReturnMessage",
        "testResult",
        "mac"
})
public class Root {

    @XmlElement(name = "I_ReturnMessage")
    protected String iReturnMessage;
    @XmlElement(name = "TestResult")
    protected String testResult;
    @XmlElement(name = "Mac")
    protected String mac;

    // --- Getters and Setters ---
    public String getiReturnMessage() {
        return iReturnMessage;
    }

    public void setiReturnMessage(String value) {
        this.iReturnMessage = value;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String value) {
        this.testResult = value;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String value) {
        this.mac = value;
    }
}
