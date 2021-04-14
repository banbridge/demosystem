package com.bupt.demosystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * Created by Banbridge on 2021/2/7.
 */
@Configuration
// @EnableWebSocketMessageBroker注解用于开启使用STOMP协议来传输基于代理（MessageBroker）的消息，这时候控制器（controller）
// 开始支持@MessageMapping,就像是使用@requestMapping一样
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 广播式配置名为 /all 消息代理 , 这个消息代理必须和 controller 中的 @SendTo 配置的地址前缀一样或者全匹配
        registry.enableSimpleBroker("/all/", "/one");
        registry.setApplicationDestinationPrefixes("/ws");
    }
}
