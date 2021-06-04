package com.bupt.demosystem.aodv;

import java.net.InetSocketAddress;

/**
 * @Author banbridge
 * @Classname RouterTable
 * @Date 2021/6/2 16:42
 */
public class RouterTable {

    /**
     * 目标ip地址,可以讲InetSocketAddress转化为String类型
     */
    private InetSocketAddress destIpAddress;

    /**
     * 目标地址序列号，每一个节点的路由表条目中都必须维护到目的节点的序列号，
     * 在三种情况下节点会更新路由表中的序列号：
     * (1)目的IP地址为自己的路由表条目。节点发送RREQ消息前，自身序列号加一，
     * 以便通知其他节点路由需要重新搜索。另外在发送回复RREQ消息的RREP消息前，把自身
     * 序列号更新为旧的序列号和RREQ中目的序列号中两者的最大值。
     * (2)节点收到AODV控制消息，即RREQ、RREP和RERR。若控制消息中的序列号比路由表中的大，
     * 即控制消息中的路由更新，此时用大的序列号更新路由表序列号；若两个序列号相等，但路
     * 由表中的跳数字段比控制消息中的跳数字段+1要大，即控制消息中的路由更短，此时用控制
     * 消息更新路由表中的相关字段，如下一跳，网络接口。
     * (3)到目的节点的路径过期或者损坏。在没有收到下一节点回复的RREP-ACK或者链路层通知发生
     * 链路损坏时，需要把所有受链路影响不可达的路由表条目中的目的序列号都加一，并设置标志
     * 位不合法，这样可以避免后续该节点重新使用损坏的链路。
     */
    private int destSequenceNumber;

    /**
     * 到达目的地所需跳数
     */
    private int hopCount;

    /**
     * 下一跳地址
     */
    private InetSocketAddress nextHop;

    /**
     * 有效的目标序列号标志
     */
    private boolean validDestSequenceNumberFlag;

    /**
     * 其他状态和路由标志
     */
    private int otherFlag;

    /**
     * 上游节点列表， 用于建立反向路由，当下游链路出现问题时可以通知上游节点
     */
    //private List<Object> precursors;

}
