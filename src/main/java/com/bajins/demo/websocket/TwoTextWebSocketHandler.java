package com.bajins.demo.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TwoTextWebSocketHandler extends TextWebSocketHandler {
    private static Map<String, WebSocketSession> map = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        map.put((String) session.getAttributes().get("id"), session);
        System.out.println("已经有一个客户端连接，当前连接数：" + map.size());
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("接收到客户端发来的消息，" + (String) session.getAttributes().get("id") + "消息为：" + message.getPayload());
        super.handleTextMessage(session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("客户端" + (String) session.getAttributes().get("id") + "出错");
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("已经有一个客户端退出，退出客户端为" + (String) session.getAttributes().get("id") + "，当前连接数：" + map.size());
        super.afterConnectionClosed(session, status);
    }
}
