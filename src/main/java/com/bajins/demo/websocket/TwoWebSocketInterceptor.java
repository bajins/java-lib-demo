package com.bajins.demo.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * WebSocket拦截器，对于访问了WebSocket的进行拦截
 *
 * @author claer claer@woytu.com
 * @program com.cyw.api.config
 * @description WebSocketInterceptor 拦截器
 * @create 2018-05-29 11:01
 */
@Component
public class TwoWebSocketInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TwoWebSocketInterceptor.class);

    /**
     * 初次握手访问前，可以把客户端传来的识别信息先鉴权，然后存储到缓存，方便之后使用
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes
     * @return
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   org.springframework.web.socket.WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            String email = servletRequest.getParameter("email");
            // 这里查询鉴权，并返回需要的用户信息，比如：用户昵称，手机号，用户id等

            // 判断用户是否存在
            if (email != null) {
                // 这里可以把查询的用户信息存放到webSocketSession
                logger.info("用户 {} 存在", email);
                attributes.put("WEBSOCKET_USER", email);
                servletRequest.getSession().setAttribute("WEBSOCKET_USER", email);
                return true;
            }
        }
        return false;
    }

    /**
     * 初次握手访问后
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param exception
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               org.springframework.web.socket.WebSocketHandler wsHandler, Exception exception) {

    }

}
