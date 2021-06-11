package com.bupt.demosystem.aodv.module;

import com.bupt.demosystem.aodv.message.RoutingTableEntry;
import com.bupt.demosystem.aodv.message.*;
import com.bupt.demosystem.aodv.message.tool.AodvMessageType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    private final MessageQueue recvMessageQueue;

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
    private HashMap<InetSocketAddress, RoutingTableEntry> routeTable;

    /**
     * 所有节点能够收到广播消息的节点
     */
    private final List<RouteNode> neighborList = new LinkedList<>();

    /**
     * 邻居节点中实际建立连接的节点
     */
    private final List<RouteNode> connectList = new LinkedList<>();

    /**
     * 节点的序列号
     */
    private int sequenceNumber;

    /**
     * 节点维护的rreq_id
     */
    private int rreq_id;

    /**
     * rreq Id的过期时间
     */
    private IdCache m_idCache;


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
        this.recvMessageQueue = new MessageQueue();
        //线程池
        this.exec = exec;
        //初始化路由表
        routeTable = new HashMap<>();
        this.sequenceNumber = 1;
        this.m_idCache = new IdCache(5000);
    }

    /**
     * 本线程用来处理消息待处理消息队列的消息 并将新的要发送消息放入发送队列
     */
    @Override
    public void run() {
        System.out.println("节点" + this.ipAddress + "启动");
        //该线程主要用来接收消息放入待处理消息队列
        exec.submit(new UdpReceive(recvMessageQueue, selector));
        //该线程将待发送消息队列的消息发送出去
        exec.submit(new UdpSend(sendMessageQueue, dc));
        //定时任务线程启动
        AodvScheduleTask aodvScheduleTask = new AodvScheduleTask();
        exec.execute(aodvScheduleTask);
        while (!Thread.interrupted()) {
            try {
                AodvMessage msg = this.recvMessageQueue.take();
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
            case RREQ:
                recvRequest(msg);
                break;
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
        }

    }

    /**
     * 如果接收到的是路由请求分组，则调用recvRequest(Packet*)函数进行处理。
     * 如果该分组由节点自身产生或已经接收过的，会被节点丢 弃，并结束处理。
     * 否则，节点将缓存该分组的序列号，并将该分组发送来的路径添加到反向路由中，转发相应分组。
     * 然后，节点根据该分组的目的地址进行判断并调 用不同函数进行处理。如果节点自身即为目的节点，
     * 则调用sendReply(nsaddr_t, u_int32_t,nsaddr_t, u_int32_t, u_int32_t, double)函数进行响应。
     * 如果节点不是目的节点，但知道通往目的节点的路由，则调用sendReply 函数进行响应，并在源和目的前驱列表
     * 中分别插入到源和目的的下一跳节点。否则，不能直接响应该请求，将跳数加1，并调用forward(aodv_rt_entry*, Packet*, double)
     * 函数转发该分组。在sendReply 函数中，节点首先查找到达目的节点（即发送路由请求分组的节点）的路由，创建并填充分组，
     * 然后调用 Scheduler::instance().schedule()函数来发送该分组。
     *
     * @param msg
     */
    private void recvRequest(AodvMessage msg) {

        RREQ recv_rreq = (RREQ) msg.getObject();
        //rreq分组为自身节点发送
        if (m_idCache.isDuplicate(recv_rreq.getOrigIpAddress(), recv_rreq.getRreqId())) {
            //丢弃该rreq分组
            System.out.printf("节点%d 丢弃rreq消息：%d ", this.nodeId, recv_rreq.getRreqId());
            return;
        }


    }

    /**
     * 如果是数据分组，则节点丢弃已经发送过或者ttl 为0 的分组，并结束处理。如果分组是由上层协议产生的，则节点添加IP 报头。随后，节点根据目的路由进行不同处理
     * （1）如果目的节点路由未知，则调用rt_resolve(Packet*)函数进行路由解析和转发。如果目的节点路由在路由表中存在，
     * 则直接调用 forward 函数进行转发。如果分组是由节点自身产生的，则将分组保存到缓冲队列中，并调用sendRequest(nsaddr_t)
     * 函数查询目的路由。如果目的路 由已知，但正在进行本地修复，则将分组保存到缓冲队列中。否则，丢弃该分组，并调用sendError 函数报错。
     * （2）如果目的节点路由已知，则调用forward 进行转发。节点丢弃ttl 为0 的分组，并根据分组类型决定下一步操作。如果接收到的是数据分组，
     * 且自身为目的节点，则通过调用PortClassifier 对象的recv(Packet*, Handle*)函数将分组交递给高层协议， 并结束处理。
     * 否则， 节点设置分组属性， 并调用Scheduler::instance().schedule (Handler*, Event*, double)函数来发送分组。
     * 其中，Handler 为基类中的属性target_（会根据脚本中的设置指向相应的协议实体）， Event 为要发送的分组即可。以上就是在节点收到分组后的一个处理过程。
     *
     * @param msg
     */
    private void receiveContent(AodvMessage msg) throws InterruptedException {

        MessageContent recvMsg = (MessageContent) msg.getObject();
        //查看消息的目的地地址是不是自己
        if (recvMsg.getToAddress().equals(this.ipAddress)) {
            //消息到达目的地址，此次消息交付上层应用处理
            System.out.println("消息到达目的节点，交付上层应用。");
        } else if (routeTable.containsKey(recvMsg.getToAddress())) {
            //生成待发送的消息
            RoutingTableEntry item = routeTable.get(recvMsg.getToAddress());
            msg.setLastHopAddress(ipAddress);
            msg.setNextHopAddress(item.getNextHop());
            putSendMessage(msg);
        } else if (recvMsg.getFromAddress().equals(this.ipAddress)) {
            //如果没有相应的路由线路，则进行路由发现RREQ
            //putUntreatedMessage(msg);
            //如果本节点是该分组的源节点，说明没有到目的节点的路，此时发送RREQ找路
            sendRequest(recvMsg.getToAddress());
        }

    }

    /**
     * 根据目的地址构建发送RREQ
     *
     * @param toAddress
     */
    private void sendRequest(InetSocketAddress toAddress) throws InterruptedException {
        //生成RREQ消息前，将RREQ ID加1
        autoIncrementRREQId();
        for (RouteNode node : neighborList) {
            RREQ rreq = new RREQ();
            AodvMessage aodvMessage = new AodvMessage();
            rreq.setRreqId(this.rreq_id);
            rreq.setDestIpAddress(toAddress);
            rreq.setOrigIpAddress(this.ipAddress);
            rreq.setOrigSequenceNumber(this.sequenceNumber);
            rreq.setFlagU(true);
            rreq.setRreqId(0);
            aodvMessage.setCreateTime(LocalTime.now());
            aodvMessage.setPacketType(AodvMessageType.RREQ);
            aodvMessage.setNextHopAddress(node.getIpAddress());
            aodvMessage.setLastHopAddress(this.ipAddress);
            aodvMessage.setObject(rreq);
            putSendMessage(aodvMessage);
        }

    }


    /**
     * 将接收到的消息放入待处理队列
     *
     * @param msg
     * @throws InterruptedException
     */
    public void putRecvMessage(AodvMessage msg) throws InterruptedException {
        this.recvMessageQueue.put(msg);
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

    public HashMap<InetSocketAddress, RoutingTableEntry> getRouteTable() {
        return routeTable;
    }

    /**
     * 自增序列号
     */
    public void autoIncrementSequenceNumber() {
        this.sequenceNumber++;
        if (this.sequenceNumber == Integer.MAX_VALUE) {
            this.sequenceNumber = 1;
        }
    }

    /**
     * 自增节点维护的rreq的id
     */
    public void autoIncrementRREQId() {
        this.rreq_id++;
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
