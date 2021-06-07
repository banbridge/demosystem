package com.bupt.demosystem.aodv.message;

import java.io.*;
import java.net.InetSocketAddress;

/**
 * @Author banbridge
 * @Classname Message
 * @Date 2021/5/27 16:26
 * 路由节点发送具体消息内容的消息格式
 */
public class MessageContent implements Serializable {

    /**
     * 源ip地址和目的ip地址
     */
    private InetSocketAddress fromAddress;
    private InetSocketAddress toAddress;

    private String message;

    public InetSocketAddress getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(InetSocketAddress fromAddress) {
        this.fromAddress = fromAddress;
    }

    public InetSocketAddress getToAddress() {
        return toAddress;
    }

    public void setToAddress(InetSocketAddress toAddress) {
        this.toAddress = toAddress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MessageContent{" +
                "fromAddress=" + fromAddress +
                ", toAddress=" + toAddress +
                ", message='" + message + '\'' +
                '}';
    }
}
