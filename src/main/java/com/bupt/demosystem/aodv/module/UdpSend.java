package com.bupt.demosystem.aodv.module;

import com.bupt.demosystem.aodv.message.AodvMessage;
import com.bupt.demosystem.aodv.message.MessageContent;
import com.bupt.demosystem.aodv.message.MessageQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @Author banbridge
 * @Classname UdpSend
 * @Date 2021/6/20 20:42
 */
public class UdpSend implements Runnable {

    private boolean runFlag;
    private DatagramChannel dc;
    private MessageQueue sendQueue;

    public UdpSend() {
    }

    public UdpSend(MessageQueue sendQueue, DatagramChannel dc) {
        runFlag = true;
        this.dc = dc;
        this.sendQueue = sendQueue;
    }

    @Override
    public void run() {
        try {
            while (this.runFlag) {
                AodvMessage message = sendQueue.take();
                MessageContent msgContent = (MessageContent) message.getObject();
                System.out.println(msgContent.getFromAddress() + "发送消息：" + message);
                this.dc.send(ByteBuffer.wrap(AodvMessage.objectToByte(message)), message.getNextHopAddress());

            }
        } catch (IOException | InterruptedException e) {
            System.out.println("发送失败");
            e.printStackTrace();
        }
    }


    public void sendUdpMessage(AodvMessage message) {


    }

    public void stop() {
        this.runFlag = false;
    }

}
