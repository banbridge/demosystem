package com.bupt.demosystem.aodv.module;

import person.gyb.aodv.message.Message;
import person.gyb.aodv.message.MessageQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @Author banbridge
 * @Classname UdpSend
 * @Date 2021/5/28 09:14
 */
public class UdpSend implements Runnable {

    private final MessageQueue sendMessageQueue;

    private final DatagramChannel datagramChannel;

    public UdpSend(MessageQueue sendMessageQueue, DatagramChannel datagramChannel) {
        this.sendMessageQueue = sendMessageQueue;
        this.datagramChannel = datagramChannel;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Message msg = sendMessageQueue.take();
                System.out.println(msg.getFromAddress() + "发送消息：" + msg);
                datagramChannel.send(ByteBuffer.wrap(Message.objectToByte(msg)), msg.getToAddress());
            } catch (InterruptedException e) {
                System.out.println("获取待发送消息失败");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("消息发送失败");
                e.printStackTrace();
            }

        }
    }
}
