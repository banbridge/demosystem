package com.bupt.demosystem.aodv.module;


import com.bupt.demosystem.aodv.message.AodvMessageType;
import com.bupt.demosystem.aodv.message.AodvMessage;
import com.bupt.demosystem.aodv.message.MessageContent;
import com.bupt.demosystem.aodv.message.MessageQueue;

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
                AodvMessage msg = sendMessageQueue.take();
                if (msg.getPacketType() == AodvMessageType.CONTENT) {
                    MessageContent msgContent = (MessageContent) msg.getObject();
                    System.out.println(msgContent.getFromAddress() + "发送消息：" + msg);
                    datagramChannel.send(ByteBuffer.wrap(AodvMessage.objectToByte(msg)), msgContent.getToAddress());
                }

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
