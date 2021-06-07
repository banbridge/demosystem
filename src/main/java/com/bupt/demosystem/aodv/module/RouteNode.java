package com.bupt.demosystem.aodv.module;

import com.bupt.demosystem.aodv.RouteTableItem;
import com.bupt.demosystem.aodv.message.AodvMessage;
import com.bupt.demosystem.aodv.message.AodvMessageType;
import com.bupt.demosystem.aodv.message.MessageContent;
import com.bupt.demosystem.aodv.message.MessageQueue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
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

public class RouteNode implements Runnable {

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
     * 路由表条目
     */
    private HashMap<InetSocketAddress, RouteTableItem> routeTable;

    /**
     * 所有节点能够收到广播消息的节点
     */
    private final List<RouteNode> neighborList = new LinkedList<>();

    /**
     * 邻居节点中实际建立连接的节点
     */
    private final List<RouteNode> connectList = new LinkedList<>();


    public RouteNode(int id, String ip, int port, ScheduledThreadPoolExecutor exec) throws IOException {
        //设置节点id
        this.nodeId = id;
        //根据ip和断喽设置标识符
        this.ipAddress = new InetSocketAddress(ip, port);
        //NIO UDP准备工作
        this.dc = DatagramChannel.open();
        this.dc.configureBlocking(false);
        this.dc.socket().bind(ipAddress);
        //选择器
        this.selector = Selector.open();
        this.dc.register(selector, SelectionKey.OP_READ);
        //初始化消息队列
        this.sendMessageQueue = new MessageQueue();
        this.untreatedMessageQueue = new MessageQueue();
        //线程池
        this.exec = exec;
        //初始化路由表
        routeTable = new HashMap<>();
    }

    /**
     * 本线程用来处理消息待处理消息队列的消息 并将新的要发送消息放入发送队列
     */
    @Override
    public void run() {
        System.out.println("节点" + this.ipAddress + "启动");
        //该线程主要用来接收消息放入待处理消息队列
        exec.submit(new UdpReceive(untreatedMessageQueue, selector));
        //该线程将待发送消息队列的消息发送出去
        exec.submit(new UdpSend(sendMessageQueue, dc));
        //定时任务线程启动
        AodvScheduleTask aodvScheduleTask = new AodvScheduleTask();
        exec.execute(aodvScheduleTask);
        while (!Thread.interrupted()) {
            try {
                AodvMessage msg = this.untreatedMessageQueue.take();
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

    public void dealMessage(AodvMessage msg) throws InterruptedException {
        /*
          处理消息
         */
        System.out.println("处理消息:" + msg.toString());
        //休眠1s模拟处理消息
        TimeUnit.SECONDS.sleep(1);
        switch (msg.getPacketType()) {
            case RREP_ACK:

                break;
            case RERR:

                break;
            case CONTENT:
                receiveContent(msg);
                break;
            case RREP:

                break;
            default:
                System.out.println("并不支持此类型消息的处理");
                ;
        }

    }

    /**
     * 如果收到的是普通的消息，则进行处理
     *
     * @param msg
     */
    private void receiveContent(AodvMessage msg) throws InterruptedException {

        MessageContent recvMsg = (MessageContent) msg.getObject();
        //查看消息的目的地地址是不是自己
        if (recvMsg.getToAddress().equals(this.ipAddress)) {
            //消息到达目的地址，此次消息交付上层应用处理
            System.out.println("消息到达目的节点");
        } else if (routeTable.containsKey(recvMsg.getToAddress())) {
            //生成待发送的消息
            RouteTableItem item = routeTable.get(recvMsg.getToAddress());
            msg.setLastHopAddress(ipAddress);
            msg.setNextHopAddress(item.getNextHop());
            putSendMessage(msg);
        } else {

        }

    }


    /**
     * 将接收到的消息放入待处理队列
     *
     * @param msg
     * @throws InterruptedException
     */
    public void putUntreatedMessage(AodvMessage msg) throws InterruptedException {
        this.untreatedMessageQueue.put(msg);
    }

    /**
     * 将处理后的消息或者新消息放入发送队列
     *
     * @param msg
     * @throws InterruptedException
     */
    public void putSendMessage(AodvMessage msg) throws InterruptedException {
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

    public HashMap<InetSocketAddress, RouteTableItem> getRouteTable() {
        return routeTable;
    }

    /**
     * 添加连接的节点
     *
     * @param rn
     */
    public void addConnectNode(RouteNode rn) {
        this.connectList.add(rn);
    }
}
