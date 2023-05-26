package com.bajins.demo.webservice;

import org.springframework.stereotype.Service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService(targetNamespace = "http://demo.webservice.bajins.com")
@Service
public class ServerService {
    @WebMethod
    @WebResult
    public String send(@WebParam String username) {
        if ("zhangsan".equals(username)) {
            return "张三";
        }
        return "李四，王五";
    }

    @WebMethod
    @WebResult
    public String message(@WebParam String message) {
        return "====Hello ====WebServer===" + message;
    }
}
