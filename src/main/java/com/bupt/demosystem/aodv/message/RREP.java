package com.bupt.demosystem.aodv.message;

import java.net.InetSocketAddress;

/**
 * @author banbridge
 * RREP消息用于单播回复RREQ消息，目的是为了告知发送RREQ消息的源节点到目
 * 的节点的路由。通过RREP消息可以建立从收到RREP消息的节点到RREP消息中的
 * 目的节点的正向路由，用于以后发送数据到目的节点。
 */
public class RREP {


    /*
    Hello消息用于活跃节点向所有邻近节点广播自身的存在，当一个节点处于正在使用的路由中时，需要定时向邻近节点广播Hello消息，若邻近节点收到Hello消息，则更新路由表中对应节点的生存时间。
    Hello消息是一种特殊的RREP消息，其特殊之处在于为某些字段设置了特殊值
    目的IP地址      设置为发送节点自身的IP地址
    目的序列号      设置为发送节点自身最新的序列号
    跳数           设置为0
    生存时间        设置为ALLOW_HELLO_LOSS * HELLO_INTERVAL
        */

    /**
     * 从源节点到目的节点经过的节点数
     */
    private int hopCount;


    /**
     * Acknowledgement Required 需要确认标志，设为1则收到该消息的写一个邻近节点需要单播RREP_ACK消息到本节点，
     * 这一做法是当链路不可靠或者只能单向传播的时候，用RREP消息可以检测出来
     */
    private boolean flagA;

    /**
     * 目标接收ip
     */
    private InetSocketAddress destIpAddress;

    /**
     * The destination sequence number associated to the route.
     */
    private int destSequenceNumber;

    /**
     * rreq发起者ip地址
     */
    private InetSocketAddress origIpAddress;

    /**
     * 节点接收的时间（ms），rrep认为路由是有效的，节点只有在这段时间前收到该条RREP信息，才承认RREP信息有效，否则则视为无效
     */
    private int lifeTime;


}
