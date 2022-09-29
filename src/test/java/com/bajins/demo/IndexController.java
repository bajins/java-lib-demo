package com.bajins.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class IndexController {

    // 通过simpMessagingTemplate向浏览器发送消息
    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @MessageMapping(value = {"", "/"})
    public String index() {
        return "index";
    }

    @MessageMapping("/test")
    public void test() {
        // messagingTemplate向用户发送消息,第一个参数事接收者,第二个事浏览器的订阅地址,第三个消息本身
        messagingTemplate.convertAndSendToUser("1", "queue/notifications", "test");
    }
}
