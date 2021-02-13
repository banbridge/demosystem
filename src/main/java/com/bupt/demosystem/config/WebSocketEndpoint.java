package com.bupt.demosystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Banbridge on 2021/2/7.
 */
@ServerEndpoint(value = "/websocket")
@Component
public class WebSocketEndpoint {

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 接收userId
     */
    private String userID = "";
    private final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    /**
     * 连接建立成功
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        String id = UUID.randomUUID().toString();
        this.session = session;
        this.userID = session.getId();
        sendMessage("服务器连接成功");
        logger.info("接收到连接:" + userID);
        onAllMessage(userID + "来了");
        SessionPool.addSession(userID, session);
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose() {
        SessionPool.removeSession(this.userID);
        logger.info("连接关闭:" + this.userID);
        onAllMessage(this.userID + "走了");
    }

    /**
     * 连接出现错误
     */
    @OnError
    public void onError(Throwable error) {
        logger.error("连接出现错误:" + this.userID);
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("用户消息:" + userID + ",报文:" + message);
    }

    /**
     * 向所有用户发消息
     */
    public static void onAllMessage(String message) {
        SessionPool.sessionMap.forEach((k, v) -> {
            try {
                v.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


}
