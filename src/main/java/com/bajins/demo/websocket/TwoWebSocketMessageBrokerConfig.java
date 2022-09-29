package com.bajins.demo.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


// @EnableWebSocketMessageBroker注解用于开启使用STOMP协议来传输基于代理（MessageBroker）的消息
// 这时候控制器（controller）开始支持@MessageMapping,就像是使用@RequestMapping一样。
@EnableWebSocketMessageBroker
@Configuration
public class TwoWebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        /*
         * 注册STOMP协议的节点(endpoint),并映射指定的url,
         * 添加一个访问端点“/endpointTest”,客户端打开双通道时需要的url,
         * 允许所有的域名跨域访问，指定使用SockJS协议。
         */
        registry.addEndpoint("/endpointTest").setAllowedOrigins("*").withSockJS();
        // 注册一个名为/endpointChat的endpoint
        registry.addEndpoint("/endpointChat").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 点对点广播式配置增加一个/queue消息代理和topic代理, 这个消息代理必须和 controller 中的 @SendTo 配置的地址前缀一样或者全匹配
        registry.enableSimpleBroker("queue", "/topic");
    }
}
