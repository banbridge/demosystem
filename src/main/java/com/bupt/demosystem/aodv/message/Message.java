package com.bupt.demosystem.aodv.message;

import java.io.*;
import java.net.InetSocketAddress;

/**
 * @Author banbridge
 * @Classname Message
 * @Date 2021/5/27 16:26
 * 路由节点发送的消息格式
 */
public class Message implements Serializable {

    private InetSocketAddress fromAddress;
    private InetSocketAddress toAddress;

    private String message;

    public static Object byteToObject(byte[] bytes) {
        Object obj = null;
        try {
            // bytearray to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);

            obj = oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception e) {
            System.out.println("translation" + e.getMessage());
            e.printStackTrace();
        }
        return obj;
    }

    public static byte[] objectToByte(Object obj) {
        byte[] bytes = null;
        try {
            // object to bytearray
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);

            bytes = bo.toByteArray();

            bo.close();
            oo.close();
        } catch (Exception e) {
            System.out.println("translation" + e.getMessage());
            e.printStackTrace();
        }
        return bytes;
    }

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
        return "Message{" +
                "fromAddress=" + fromAddress +
                ", toAddress=" + toAddress +
                ", message='" + message + '\'' +
                '}';
    }
}
