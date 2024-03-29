package com.bupt.demosystem.aodv.module;

import com.bupt.demosystem.aodv.message.RoutingTableEntry;
import com.bupt.demosystem.aodv.message.*;
import com.bupt.demosystem.aodv.message.tool.AodvMessageType;
import org.springframework.data.relational.core.sql.In;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
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
     * 线程运行标志
     */
    private boolean flag_run;

    /**
     * udp接收到消息后会放到，待处理的message队列，等待其他线程处理
     */
    private final MessageQueue recvMessageQueue;
    private final MessageQueue sendMessageQueue;

    /// A "drop-front" queue used by the routing layer to buffer packets to which it does not have a route.
    private final MessageQueue queue;


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
    private RoutingTable routingTable;

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
    private int seqNo;

    /**
     * 节点维护的rreq_id
     */
    private int requestId;

    /**
     * rreq Id的过期时间
     * Handle duplicated RREQ
     */
    private IdCache rreqIdCache;

    /**
     * Handle neighbors
     */
    private RoutingNeighbor nb;

    /**
     * Number of RREQs used for RREQ rate control
     */
    private int rreqCount;

    /**
     * Number of RERRs used for RERR rate control
     */
    private int rerrCount;

    /**
     * Protocol parameters.
     */

    /**
     * Maximum number of retransmissions of RREQ with TTL = NetDiameter to discover a route
     */
    private int rreqRetries;
    /**
     * Initial TTL value for RREQ.
     */
    private int ttlStart;
    /**
     * TTL increment for each attempt using the expanding ring search for RREQ dissemination.
     */
    private int ttlIncrement;
    /**
     * Maximum TTL value for expanding ring search, TTL = NetDiameter is used beyond this value.
     */
    private int ttlThreshold;
    /**
     * Provide a buffer for the timeout.
     */
    private int timeoutBuffer;
    /**
     * Maximum number of RREQ per second.
     */
    private int rreqRateLimit;
    /**
     * Maximum number of REER per second.
     */
    private int rerrRateLimit;
    /**
     * Period of time during which the route is considered to be valid.
     */
    private int activeRouteTimeout;
    /**
     * Net diameter measures the maximum possible number of hops between two nodes in the network
     */
    private int netDiameter;

    /**
     * NodeTraversalTime is a conservative estimate of the average one hop traversal time for packets
     * and should include queuing delays, interrupt processing times and transfer times.
     */
    private int nodeTraversalTime;
    /*8
    Estimate of the average net traversal time.
     */
    private int netTraversalTime;
    /**
     * Estimate of maximum time needed to find route in network.
     */
    private int pathDiscoveryTime;
    /**
     * Value of lifetime field in RREP generating by this node.
     */
    private int myRouteTimeout;
    /**
     * Every HelloInterval the node checks whether it has sent a broadcast  within the last HelloInterval.
     * If it has not, it MAY broadcast a  Hello message
     */
    private int helloInterval;
    /**
     * Number of hello messages which may be loss for valid link
     */
    private int allowedHelloLoss;
    /**
     * DeletePeriod is intended to provide an upper bound on the time for which an upstream node A
     * can have a neighbor B as an active next hop for destination D, while B has invalidated the route to D.
     */
    private int deletePeriod;
    /**
     * Period of our waiting for the neighbour's RREP_ACK
     */
    private int nextHopWait;
    /**
     * Time for which the node is put into the blacklist
     */
    private int blackListTimeout;
    /**
     * The maximum number of packets that we allow a routing protocol to buffer.
     */
    private int maxQueueLen;
    /**
     * The maximum period of time that a routing protocol is allowed to buffer a packet for.
     */
    private LocalTime maxQueueTime;
    /**
     * Indicates only the destination may respond to this RREQ.
     */
    private boolean destinationOnly;
    /**
     * Indicates whether a gratuitous RREP should be unicast to the node originated route discovery.
     */
    private boolean gratuitousReply;
    /**
     * Indicates whether a hello messages enable
     */
    private boolean enableHello;
    /**
     * Indicates whether a a broadcast data packets forwarding enable
     */
    private boolean enableBroadcast;

    /**
     * Keep track of the last bcast time
     */
    private int lastBcastTime;

    private UdpReceive udpReceive;
    private UdpSend udpSend;

    /**
     * Hello timer
     */
    private ScheduledFuture<?> helloTimer;

    /**
     * RREQ rate limit timer
     */
    private ScheduledFuture<?> rreqRateLimitTimer;

    /**
     * RERR rate limit timer
     */
    private ScheduledFuture<?> rerrRateLimitTimer;

    public RouteNode(int id, String ip, int port, ScheduledThreadPoolExecutor exec) throws IOException {
        //设置节点id
        this.nodeId = id;
        this.flag_run = true;
        //根据ip和断喽设置标识符
        this.ipAddress = new InetSocketAddress(ip, port);
        //NIO UDP准备工作
        this.dc = DatagramChannel.open();
        this.dc.configureBlocking(false);
        this.dc.socket().bind(ipAddress);
        //选择器
        this.selector = Selector.open();
        this.dc.register(selector, SelectionKey.OP_READ);
        this.recvMessageQueue = new MessageQueue();
        this.sendMessageQueue = new MessageQueue();
        this.queue = new MessageQueue();
        //线程池
        this.exec = exec;


        this.rreqRetries = 2;
        this.ttlStart = 1;
        this.ttlIncrement = 2;
        this.ttlThreshold = 7;
        this.timeoutBuffer = 2;
        this.rreqRateLimit = 10;
        this.rerrRateLimit = 10;
        //3s
        this.activeRouteTimeout = 3000;
        this.netDiameter = 35;
        //40ms
        this.nodeTraversalTime = 40;
        this.netTraversalTime = (2 * netDiameter) * nodeTraversalTime;
        this.pathDiscoveryTime = 2 * netTraversalTime;
        this.myRouteTimeout = 2 * Math.max(pathDiscoveryTime, activeRouteTimeout);
        //1s
        this.helloInterval = 1000;
        this.allowedHelloLoss = 2;
        this.deletePeriod = 5 * Math.max(activeRouteTimeout, helloInterval);
        this.nextHopWait = nodeTraversalTime + 10;
        this.blackListTimeout = rreqRetries * netTraversalTime;
        this.destinationOnly = false;
        this.gratuitousReply = true;
        this.enableHello = false;
        //初始化路由表
        this.routingTable = new RoutingTable(deletePeriod);
        this.requestId = 0;
        this.seqNo = 0;
        this.rreqIdCache = new IdCache(pathDiscoveryTime);
        this.nb = new RoutingNeighbor(helloInterval, exec);
        this.rreqCount = 0;
        this.rerrCount = 0;
        this.lastBcastTime = 0;

    }


    /**
     * 本线程用来处理消息待处理消息队列的消息 并将新的要发送消息放入发送队列
     */
    @Override
    public void run() {
        System.out.println("节点" + this.ipAddress + "启动");
        //该线程主要用来接收消息放入待处理消息队列
        this.udpReceive = new UdpReceive(recvMessageQueue, selector);
        this.udpSend = new UdpSend(sendMessageQueue, dc);
        exec.submit(udpReceive);
        exec.submit(udpSend);
        while (flag_run) {
            try {
                AodvMessage msg = this.recvMessageQueue.take();
                dealMessage(msg);
            } catch (InterruptedException e) {
                System.out.println("untreatedMessageQueue消息获取失败。。。。。");
                e.printStackTrace();
            }
        }
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

    public void start() {
        if (this.enableHello) {
            this.nb.scheduleTimer();
        }

    }

    /**
     * if route exits and is valid, forward packet
     *
     * @return
     */
    boolean forwarding(AodvMessage msg) {
        return false;
    }

    public void dealMessage(AodvMessage msg) throws InterruptedException {
        /*
          处理消息
         */
        System.out.println("处理消息:" + msg.toString());
        //休眠1s模拟处理消息
        //TimeUnit.SECONDS.sleep(1);
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

        RreqHeader recv_rreq = (RreqHeader) msg.getObject();
        //rreq分组为自身节点发送
        if (this.rreqIdCache.isDuplicate(recv_rreq.getOrigin(), recv_rreq.getRequestID())) {
            //丢弃该rreq分组
            System.out.printf("节点%d 丢弃rreq消息：%d ", this.nodeId, recv_rreq.getRequestID());
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
            return;
        }
        RoutingTableEntry item = routingTable.lookupRoute(recvMsg.getToAddress());
        if (item != null) {
            //生成待发送的消息
            msg.setLastHopAddress(ipAddress);
            msg.setNextHopAddress(item.getNextHop());
            putSendMessage(msg);
            return;
        }
        if (recvMsg.getFromAddress().equals(this.ipAddress)) {
            //如果没有相应的路由线路，则进行路由发现RREQ
            //putUntreatedMessage(msg);
            //如果本节点是该分组的源节点，说明没有到目的节点的路，此时发送RREQ找路

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
     * 将消息放入发送队列
     *
     * @param msg
     * @throws InterruptedException
     */
    public void putSendMessage(AodvMessage msg) throws InterruptedException {
        this.sendMessageQueue.put(msg);
    }

    ///\name Receive control packets
    //\{
    /// Receive and process control packet
    public void recvAodv(AodvMessage msg) {

    }

    /**
     * Queue packet and send route request
     * <p>
     * \param p the packet to route
     * \param header the IP header
     * \param ucb the UnicastForwardCallback function
     * \param ecb the ErrorCallback function
     */
    public void DeferredRouteOutput(AodvMessage msg) {

    }

    /**
     * If route exists and is valid, forward packet.
     * <p>
     * \param p the packet to route
     * \param header the IP header
     * \param ucb the UnicastForwardCallback function
     * \param ecb the ErrorCallback function
     * \returns true if forwarded
     */
    public boolean Forwarding(AodvMessage msg) {
        return false;
    }

    /**
     * Repeated attempts by a source node at route discovery for a single destination
     * use the expanding ring search technique.
     * \param dst the destination IP address
     */
    public void ScheduleRreqRetry(InetSocketAddress dst) {

    }

    /**
     * Set lifetime field in routing table entry to the maximum of existing lifetime and lt, if the entry exists
     * \param addr - destination address
     * \param lt - proposed time for lifetime field in routing table entry for destination with address addr.
     * \return true if route to destination address addr exist
     */
    public boolean UpdateRouteLifeTime(InetSocketAddress addr, int lt) {
        return false;
    }

    /**
     * Update neighbor record.
     * \param receiver is supposed to be my interface
     * \param sender is supposed to be IP address of my neighbor.
     */
    public void UpdateRouteToNeighbor(InetSocketAddress sender, InetSocketAddress receiver) {

    }

    /**
     * Test whether the provided address is assigned to an interface on this node
     * \param src the source IP address
     * \returns true if the IP address is the node's IP address
     */
    public boolean IsMyOwnAddress(InetSocketAddress src) {
        return false;
    }


    /**
     * Process hello message
     * <p>
     * \param rrepHeader RREP message header
     * \param receiverIfaceAddr receiver interface IP address
     */
    public void ProcessHello(RrepHeader rrepHeader, InetSocketAddress receiverIfaceAddr) {

    }

    /**
     * Create loopback route for given header
     * <p>
     * \param header the IP header
     * \param oif the output interface net device
     * \returns the route
     */
    //Ptr<Ipv4Route> LoopbackRoute (const Ipv4Header & header, Ptr<NetDevice> oif) const;


    /// Receive RREQ
    public void RecvRequest(AodvMessage p, InetSocketAddress receiver, InetSocketAddress src) {

    }

    /// Receive RREP
    public void RecvReply(AodvMessage p, InetSocketAddress my, InetSocketAddress src) {

    }

    /// Receive RREP_ACK
    public void RecvReplyAck(InetSocketAddress neighbor) {

    }

    /// Receive RERR from node with address src
    public void RecvError(AodvMessage p, InetSocketAddress src) {

    }
    //\}

    ///\name Send
    //\{
    /// Forward packet from route request queue
    public void SendPacketFromQueue(InetSocketAddress dst) {

    }

    /// Send hello
    public void SendHello() {

    }

    /// Send RREQ
    public void SendRequest(InetSocketAddress dst) {
        if (this.rreqCount == rreqRateLimit) {
            return;
        } else {
            this.rreqCount++;
        }

    }

    /// Send RREP
    public void SendReply(RreqHeader rreqHeader, RoutingTableEntry toOrigin) {

    }

    /**
     * Send RREP by intermediate node
     * \param toDst routing table entry to destination
     * \param toOrigin routing table entry to originator
     * \param gratRep indicates whether a gratuitous RREP should be unicast to destination
     */
    public void SendReplyByIntermediateNode(RoutingTableEntry toDst, RoutingTableEntry toOrigin, boolean gratRep) {

    }

    /// Send RREP_ACK
    public void SendReplyAck(InetSocketAddress neighbor) {

    }

    /// Initiate RERR
    public void SendRerrWhenBreaksLinkToNextHop(InetSocketAddress nextHop) {

    }

    /// Forward RERR
    public void SendRerrMessage(AodvMessage packet, List<InetSocketAddress> precursors) {

    }

    /**
     * Send RERR message when no route to forward input packet. Unicast if there is reverse route to originating node, broadcast otherwise.
     * \param dst - destination node IP address
     * \param dstSeqNo - destination node sequence number
     * \param origin - originating node IP address
     */
    public void SendRerrWhenNoRouteToForward(InetSocketAddress dst, int dstSeqNo, InetSocketAddress origin) {

    }
    /// @}

    /**
     * Send packet to destination scoket
     * \param socket - destination node socket
     * \param packet - packet to send
     * \param destination - destination node IP address
     */
    public void SendTo(AodvMessage socket, AodvMessage packet, InetSocketAddress destination) {

    }

    /// Hello timer
    //Timer htimer;
    /// Schedule next send of hello message
    public void HelloTimerExpire() {

    }

    /// RREQ rate limit timer
    //Timer rreqRateLimitTimer;
    /// Reset RREQ count and schedule RREQ rate limit timer with delay 1 sec.
    public void RreqRateLimitTimerExpire() {

    }

    /// RERR rate limit timer
    //Timer rerrRateLimitTimer;
    /// Reset RERR count and schedule RERR rate limit timer with delay 1 sec.
    public void RerrRateLimitTimerExpire() {

    }
    /// Map IP address + RREQ timer.
    //std::map<Ipv4Address, Timer> addressReqTimer;

    /**
     * Handle route discovery process
     * \param dst the destination IP address
     */
    public void RouteRequestTimerExpire(InetSocketAddress dst) {

    }

    /**
     * Mark link to neighbor node as unidirectional for blacklistTimeout
     * <p>
     * \param neighbor the IP address of the neightbor node
     * \param blacklistTimeout the black list timeout time
     */
    public void AckTimerExpire(InetSocketAddress neighbor, int blacklistTimeout) {

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
     * 自增序列号
     */
    public void autoIncrementSequenceNumber() {
        this.seqNo++;
        if (this.seqNo >= Integer.MAX_VALUE) {
            this.seqNo = 1;
        }
    }

    /**
     * 自增节点维护的rreq的id
     */
    public void autoIncrementRREQId() {
        this.requestId++;
        if (this.requestId >= Integer.MAX_VALUE) {
            this.requestId = 1;
        }
    }

    /**
     * 添加连接的节点
     *
     * @param rn
     */
    public void addConnectNode(RouteNode rn) {
        this.connectList.add(rn);
    }

    public void stopRouteNode() {
        this.flag_run = false;
        this.udpReceive.stop();
        this.udpSend.stop();
    }




}
