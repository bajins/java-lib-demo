package com.bajins.demo.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类，设置访问URL
 *
 * @author claer admin@bajins.com
 * @program com.cyw.api.config.websocket
 * @description WebSocketConfig
 * @create 2018-05-29 11:51
 */

@Configuration
@EnableWebMvc
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurationSupport implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        /**
         * 用来注册WebSocketServer实现类，第二个参数是访问WebSocket的地址
         * springWebSocket4.1.5版本前默认支持跨域访问，之后的版本默认不支持跨域，需要设置setAllowedOrigins
         * 用来设置来自哪些域名的请求可访问，默认为localhost
         */
        registry.addHandler(webSocketHandler(), "/websocket").addInterceptors(myInterceptor())
                .setAllowedOrigins("*");

        registry.addHandler(webSocketHandler(), "/socktjs").addInterceptors(myInterceptor()
        ).withSockJS();//指定使用SockJS协议
    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new WebSocketHandler();
    }

    //websocket握手拦截器
    @Bean
    public WebSocketInterceptor myInterceptor() {
        return new WebSocketInterceptor();
    }

}
