package com.bupt.demosystem.aodv.module;

import com.bupt.demosystem.aodv.message.AodvMessage;
import com.bupt.demosystem.aodv.message.MessageContent;
import com.bupt.demosystem.aodv.message.MessageQueue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * @Author banbridge
 * @Classname UdpReceive
 * @Date 2021/5/27 17:38
 */
public class UdpReceive implements Runnable {

    /**
     * 将接收的消息放入里边
     */
    private final MessageQueue recvMessageQueue;

    private boolean flag_run;
    /**
     * 节点传来的选择器
     */
    private final Selector selector;

    private final int BUFFER_CAPACITY = 1024 * 3;


    public UdpReceive(MessageQueue recvMessageQueue, Selector selector) {
        this.recvMessageQueue = recvMessageQueue;
        this.selector = selector;
        this.flag_run = true;
    }

    @Override
    public void run() {
        try {
            while (flag_run) {

                if (selector.select(1) == 0) {
                    continue;
                }
                handReceive();

            }
        } catch (IOException | InterruptedException e) {

            e.printStackTrace();
        }

    }

    private void handReceive() throws IOException, InterruptedException {

        Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);
        byte[] msgBytes = new byte[BUFFER_CAPACITY];
        while (iter.hasNext()) {
            SelectionKey sk = iter.next();
            if (sk.isReadable()) {
                byteBuffer.clear();
                DatagramChannel datagramChannel = (DatagramChannel) sk.channel();
                InetSocketAddress address = (InetSocketAddress) datagramChannel.receive(byteBuffer);
                byteBuffer.flip();
                byteBuffer.get(msgBytes, 0, byteBuffer.remaining());
                AodvMessage msg = (AodvMessage) AodvMessage.byteToObject(msgBytes);
                byteBuffer.clear();
                recvMessageQueue.put(msg);
                System.out.println(((MessageContent) msg.getObject()).getToAddress() + "收到消息:" + msg.toString());
            }
            iter.remove();
        }

    }

    public void stop() {
        this.flag_run = false;
    }
}
