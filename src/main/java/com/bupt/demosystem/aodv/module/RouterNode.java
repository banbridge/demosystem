package com.bupt.demosystem.aodv.module;

import com.bupt.demosystem.aodv.message.Message;
import com.bupt.demosystem.aodv.message.MessageQueue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author banbridge
 * @Classname RouterNode
 * @Date 2021/5/27 16:33
 * 路由节点
 */

public class RouterNode implements Runnable {

    /**
     * 每个节点的唯一id标识符
     */
    private final int nodeId;

    /**
     * udp接收到消息后会放到，待处理的message队列，等待其他线程处理
     */
    private final MessageQueue untreatedMessageQueue;

    /**
     * 会将待发送的消息放入该队列，等待去发送
     */
    private final MessageQueue sendMessageQueue;

    /**
     * 节点的经纬度和高度
     */
    private int longitude;
    private int latitude;
    private int height;

    /**
     * 节点的ip和端口包裹类
     */
    private InetSocketAddress ipAddress;

    /**
     * 线程池的返回对象
     */
    private ScheduledThreadPoolExecutor exec;

    /**
     * udp发送消息的通道和选择器
     */
    private DatagramChannel dc;
    private Selector selector;

    /**
     * 所有节点能够收到广播消息的节点
     */
    private final List<RouterNode> neighborList = new LinkedList<>();
    /**
     * 邻居节点中实际建立连接的节点
     */
    private final List<RouterNode> connectList = new LinkedList<>();

    public RouterNode(int id, String ip, int port, ScheduledThreadPoolExecutor exec) throws IOException {
        this.nodeId = id;
        this.ipAddress = new InetSocketAddress(ip, port);
        this.dc = DatagramChannel.open();
        this.dc.configureBlocking(false);
        this.dc.socket().bind(ipAddress);
        //选择器
        this.selector = Selector.open();
        this.dc.register(selector, SelectionKey.OP_READ);
        this.sendMessageQueue = new MessageQueue();
        this.untreatedMessageQueue = new MessageQueue();
        this.exec = exec;
    }

    @Override
    public void run() {
        System.out.println("节点" + this.ipAddress + "启动");
        exec.submit(new UdpReceive(untreatedMessageQueue, selector));
        exec.submit(new UdpSend(sendMessageQueue, dc));
        while (!Thread.interrupted()) {
            try {
                Message msg = this.untreatedMessageQueue.take();
                dealMessage(msg);
            } catch (InterruptedException e) {
                System.out.println("untreatedMessageQueue消息获取失败。。。。。");
                e.printStackTrace();
            }
        }
        if (Thread.interrupted()) {
            try {
                this.dc.close();
            } catch (IOException e) {
                System.out.println("通道关闭失败");
                e.printStackTrace();
            }
            try {
                this.selector.close();
            } catch (IOException e) {
                System.out.println("选择器关闭失败");
                e.printStackTrace();
            }
        }
    }

    public void dealMessage(Message msg) throws InterruptedException {
        /*
          处理消息
         */
        System.out.println("处理消息:" + msg.toString());
        //休眠1s模拟处理消息
        TimeUnit.SECONDS.sleep(1);
        Message sendMsg = new Message();
        sendMsg.setMessage("消息：" + UUID.randomUUID());
        sendMsg.setFromAddress(this.ipAddress);
        sendMsg.setToAddress(connectList.get(0).getIpAddress());
        System.out.println("放入发送队列:" + sendMsg.toString());
        putSendMessage(sendMsg);
    }

    /**
     * 将接收到的消息放入待处理队列
     *
     * @param msg
     * @throws InterruptedException
     */
    public void putUntreatedMessage(Message msg) throws InterruptedException {
        this.untreatedMessageQueue.put(msg);
    }

    /**
     * 将处理后的消息或者新消息放入发送队列
     *
     * @param msg
     * @throws InterruptedException
     */
    public void putSendMessage(Message msg) throws InterruptedException {
        this.sendMessageQueue.put(msg);
    }

    public InetSocketAddress getIpAddress() {
        return ipAddress;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getNodeId() {
        return nodeId;
    }

    /**
     * 添加连接的节点
     *
     * @param rn
     */
    public void addConnectNode(RouterNode rn) {
        this.connectList.add(rn);
    }
}
