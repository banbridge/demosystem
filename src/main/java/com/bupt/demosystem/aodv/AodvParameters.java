package com.bupt.demosystem.aodv;

/**
 * @Author banbridge
 * @Classname AodvParameters
 * @Date 2021/6/2 16:16
 */
public class AodvParameters {

    /**
     * ACTIVE_ROUTE_TIMEOUT 3,000 毫秒
     */
    public static final int ACTIVE_ROUTE_TIMEOUT = 3000;

    /**
     * ALLOWED_HELLO_LOSS 2
     */
    public static final int ALLOWED_HELLO_LOSS = 2;

    /**
     * NODE_TRAVERSAL_TIME = 40 毫秒
     */
    public static final int NODE_TRAVERSAL_TIME = 40;
    public final static int RERR_RATELIMIT = 10;
    public static final int RREQ_RETRIES = 2;
    public static final int RREQ_RATELIMIT = 10;
    public static final int TIMEOUT_BUFFER = 0;
    public static final int TTL_ = 1;
    public static final int TTL_INCREMENT = 2;
    public static final int TTL_THRESHOLD = 7;
    public static final int TTL_VALUE = 5;
    public static final int NET_DIAMETER = 5;
    /**
     * HELLO_INTERVAL 1,000 毫秒
     */
    public static final int HELLO_INTERVAL = 1000;

    /**
     * MAX_REPAIR_TTL 0.3 * NET_DIAMETER
     */
    public static final double MAX_REPAIR_TTL = 0.3 * NET_DIAMETER;

    /**
     * MY_ROUTE_TIMEOUT 2 * ACTIVE_ROUTE_TIMEOUT
     */
    public static final int MY_ROUTE_TIMEOUT = 2 * ACTIVE_ROUTE_TIMEOUT;

    /**
     * NET_TRAVERSAL_TIME 2 * NODE_TRAVERSAL_TIME * NET_DIAMETER
     */
    public static final int NET_TRAVERSAL_TIME = 2 * NODE_TRAVERSAL_TIME * NET_DIAMETER;
    public static final int PATH_DISCOVERY_TIME = 2 * NET_TRAVERSAL_TIME;
    public static final int NEXT_HOP_WAIT = NODE_TRAVERSAL_TIME + 10;

    public static final int RING_TRAVERSAL_TIME = 2 * NODE_TRAVERSAL_TIME * (TTL_VALUE + TIMEOUT_BUFFER);
    /**
     * BLACKLIST_TIMEOUT RREQ_RETRIES * NET_TRAVERSAL_TIME
     */
    public static final int BLACKLIST_TIMEOUT = RREQ_RETRIES * NET_TRAVERSAL_TIME;



    /*
     10 . 配置参数
        本节给出了一些重要参数的默认值与 AODV 协议操作相关联。特定的移动节点
        可能希望更改某些参数，特别是NET_DIAMETER、MY_ROUTE_TIMEOUT、ALLOWED_HELLO_LOSS、RREQ_RETRIES 和
        可能是 HELLO_INTERVAL。在后一种情况下，节点应该在它的 Hello 消息中通告 HELLO_INTERVAL，通过附加一个
        RREP 消息的问候间隔扩展。选择这些 参数可能会影响协议的性能。改变
        NODE_TRAVERSAL_TIME 也会改变节点对NET_TRAVERSAL_TIME，因此只有具备适当的知识才能完成
        关于 ad hoc 网络中其他节点的行为。这MY_ROUTE_TIMEOUT 的配置值必须至少为 2 *PATH_DISCOVERY_TIME。

        参数名称值
         --------------- -----
        ACTIVE_ROUTE_TIMEOUT 3,000 毫秒
        ALLOWED_HELLO_LOSS 2
        BLACKLIST_TIMEOUT RREQ_RETRIES * NET_TRAVERSAL_TIME
        DELETE_PERIOD 请参阅下面的注释
        HELLO_INTERVAL 1,000 毫秒
        LOCAL_ADD_TTL 2
        MAX_REPAIR_TTL 0.3 * NET_DIAMETER
        MIN_REPAIR_TTL 见下面的注释
        MY_ROUTE_TIMEOUT 2 * ACTIVE_ROUTE_TIMEOUT
        净直径 35
        NET_TRAVERSAL_TIME 2 * NODE_TRAVERSAL_TIME * NET_DIAMETER
        NEXT_HOP_WAIT NODE_TRAVERSAL_TIME + 10
        NODE_TRAVERSAL_TIME 40 毫秒
        PATH_DISCOVERY_TIME 2 * NET_TRAVERSAL_TIME
        RERR_RATELIMIT 10
        RING_TRAVERSAL_TIME 2 * NODE_TRAVERSAL_TIME *
        (TTL_VALUE + TIMEOUT_BUFFER)
        RREQ_RETRIES 2
        RREQ_RATELIMIT 10
        TIMEOUT_BUFFER 2
        TTL_开始 1
        TTL_INCREMENT 2
        TTL_THRESHOLD 7
        TTL_VALUE 见下面的注释

        MIN_REPAIR_TTL 应该是最后一个已知的跳数
        目的地。如果使用 Hello 消息，则
        ACTIVE_ROUTE_TIMEOUT 参数值必须大于该值
        (ALLOWED_HELLO_LOSS * HELLO_INTERVAL)。对于给定的
        ACTIVE_ROUTE_TIMEOUT 值，这可能需要对
        HELLO_INTERVAL 的值，因此使用 Hello
        Hello 消息中的间隔扩展。

        TTL_VALUE 是 IP 头中 TTL 字段的值，而
        正在执行扩展环搜索。这将进一步描述
        在第 6.4 节中。TIMEOUT_BUFFER 是可配置的。它的目的是
        为超时提供缓冲区，以便如果 RREP 延迟
        由于拥塞，在 RREP 时发生超时的可能性较小
        仍在返回源头的途中。要省略此缓冲区，请设置
        TIMEOUT_BUFFER = 0。

        DELETE_PERIOD 旨在提供时间的上限
        上游节点 A 可以有一个邻居 B 作为活动的下一跳
        对于目的地 D，而 B 已使到 D 的路线无效。 超越
        这一次 B 可以删除到 D 的（已经失效的）路由。
        上限的确定在某种程度上取决于
        底层链路层的特性。如果 Hello 消息是
        用于确定下一跳链路的持续可用性
        节点，DELETE_PERIOD 必须至少为 ALLOWED_HELLO_LOSS *
        你好_时间间隔。如果链路层反馈用于检测丢失
        链接，DELETE_PERIOD 必须至少为 ACTIVE_ROUTE_TIMEOUT。如果你好
        从邻居接收消息，但数据包
        邻居丢失（例如，由于临时链路不对称），我们必须
        对底层链路层做更具体的假设。我们
        假设这种不对称不能持续超过一定时间，例如，
        HELLO_INTERVAL 的倍数 K。换句话说，一个节点将
        总是至少收到 K 个后续 Hello 消息中的一个
        来自邻居，如果链路正常工作并且邻居发送不
        其他交通。涵盖所有可能，

              DELETE_PERIOD = K * 最大值（ACTIVE_ROUTE_TIMEOUT，HELLO_INTERVAL）
                                 （推荐 K = 5）。

        NET_DIAMETER 测量两个之间的最大可能跳数
        网络中的节点。NODE_TRAVERSAL_TIME 是保守估计
        数据包的平均一跳遍历时间，应包括
        排队延迟、中断处理时间和传输时间。
        ACTIVE_ROUTE_TIMEOUT 应该设置为更长的值（至少 10,000
        毫秒）如果链路层指示用于检测链路
        IEEE 802.11 [ 5 ] 标准中的破坏。TTL_START 应该是
        如果 Hello 消息用于本地连接，则设置为至少 2
        信息。AODV 协议的性能对
        这些常数的选择值，通常取决于
        底层链路层协议的特性，无线电
        技术等 BLACKLIST_TIMEOUT 应适当增加，如果
        使用扩展环搜索。在这种情况下，应该
        {[(TTL_THRESHOLD - TTL_START)/TTL_INCREMENT] + 1 + RREQ_RETRIES} *
        NET_TRAVERSAL_TIME。这是为了考虑可能的额外路线
        发现尝试。

     */


}
