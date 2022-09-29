package com.bajins.demo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket处理器，
 * AbstractWebSocketHandler实现了WebSocketHandler接口，
 * 该对象提供了客户端连接,关闭,错误,发送等方法,重写这几个方法即可实现自定义业务逻辑
 *
 * @author claer claer@woytu.com
 * @program com.bajins.api.config
 * @description WebSocketHandler WebSocket处理器
 * @create 2018-05-29 11:03
 */
public class TwoWebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(TwoWebSocketHandler.class);

    // 存储在线用户，可以使用缓存
    private static final Map<String, WebSocketSession> users = new ConcurrentHashMap<>();

    /**
     * 初次链接成功后执行，对应@OnOpen
     * 这里的WebSocketSession里存储的就是拦截器在beforeHandshake方法里存储的内容
     *
     * @param webSocketSession
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        String email = (String) webSocketSession.getAttributes().get("WEBSOCKET_USER");
        // 初次连接成功就代表上线了，存在缓存中，方便发消息和统计
        users.put(email, webSocketSession);
        logger.info("{} 上线，当前在线数量:{}", email, users.size());

        if (email != null) {
            // 这里可以从消息表或缓存中查询未读消息，然后发送给客户端
            Map<String, Object> c = new HashMap<String, Object>();
            c.put("count", 10);
            c.put("msg", "你有 10 条未读消息");

            Map<String, Object> Map = new HashMap<String, Object>();
            Map.put("type", "unHandMsg");
            Map.put("material", c);
            // 转为json字符串
            String s = new ObjectMapper().writeValueAsString(Map);
            // 发送消息
            webSocketSession.sendMessage(new TextMessage(s));
        }
        // 实现自己业务，比如，当用户登录后，会把离线消息推送给用户
        //TextMessage returnMessage = new TextMessage("你将收到的离线");
        //webSocketSession.sendMessage(returnMessage);
    }

    /**
     * 发送消息，对应@OnMessage
     *
     * @param webSocketSession
     * @param webSocketMessage
     */
    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws IOException {
        // 这里接收到消息，可以存储到消息表中
        logger.info("收到消息：{}", webSocketMessage.getPayload());

        // 给所有在线用户发送消息
        TextMessage all = new TextMessage("大家好");
        Iterator<Map.Entry<String, WebSocketSession>> userIterator = users.entrySet().iterator();
        while (userIterator.hasNext()) {
            Map.Entry<String, WebSocketSession> user = userIterator.next();
            WebSocketSession wss = user.getValue();
            // 必须要在线才能发送消息
            if (wss.isOpen()) {
                wss.sendMessage(all);
            }
            // 否则从在线列表里移除
            else {
                userIterator.remove();
            }
        }

        // 发送消息给指定用户
        // 实际使用中，这里应该是用户信息对象
        String email = (String) webSocketSession.getAttributes().get("WEBSOCKET_USER");
        WebSocketSession wss = users.get("test@test.com");
        // 不能给自己发送消息，同时接收方和发送方都必须在线
        if (!email.equals("test@test.com") && wss.isOpen() && webSocketSession.isOpen()) {
            wss.sendMessage(new TextMessage("你好"));
        }
    }

    /**
     * 消息发送错误处理，对应@OnError
     *
     * @param webSocketSession
     * @param throwable
     */
    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) {
        // 实际使用中，这里应该是用户信息对象
        String email = (String) webSocketSession.getAttributes().get("WEBSOCKET_USER");

        logger.error("用户：{} 链接出错，关闭链接......", email);

        Iterator<Map.Entry<String, WebSocketSession>> userIterator = users.entrySet().iterator();
        while (userIterator.hasNext()) {
            Map.Entry<String, WebSocketSession> user = userIterator.next();
            // 把当前离线的用户从在线列表中剔除
            if (user.getKey().equals(email) && !webSocketSession.isOpen()) {
                userIterator.remove();
            }
        }
    }

    /**
     * 关闭连接后触发，对应@OnClose
     * 这里可以自定义业务逻辑，比如：给是好友关系的发送消息提示该用户已经离线
     *
     * @param webSocketSession
     * @param closeStatus
     */
    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        // 实际使用中，这里应该是用户信息对象
        String email = (String) webSocketSession.getAttributes().get("WEBSOCKET_USER");

        logger.info("用户：{} 已退出！", email);

        Iterator<Map.Entry<String, WebSocketSession>> userIterator = users.entrySet().iterator();
        while (userIterator.hasNext()) {
            Map.Entry<String, WebSocketSession> user = userIterator.next();
            // 把当前离线的用户从在线列表中剔除
            if (user.getKey().equals(email) && !webSocketSession.isOpen()) {
                userIterator.remove();
            }
        }
        logger.info("剩余在线用户:" + users.size());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


    /**
     * 客户端调用websocket.send时候，会调用该方法
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
    }


}
