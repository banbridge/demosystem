package com.bupt.demosystem.aodv.message;

import java.net.InetSocketAddress;

/**
 * @author banbridge
 * 当链路发生故障导致一个或者多个目的节点不可达时，RERR消息就会被发送，
 * 设计RERR消息是为了能通知网络中其他节点哪些目的节点因为故障导致不可访问，
 */
public class RERR extends MessageHead {

    /**
     * 本条RERR消息所通知的因为故障导致不可达的节点的数量，这个值必须大于等于1，
     * 这个值决定了后面额外的不可大目的IP得治序列号的树木
     * The number of unreachable destinations included in the message; MUST be at least 1.
     */
    private int destCount;

    /**
     * The IP address of the destination that has become unreachable due to a link break.
     */
    private InetSocketAddress unreachableDestIpAddress;

    /**
     * The sequence number in the route table entry for
     * the destination listed in the previous Unreachable
     * Destination IP Address field.
     */
    private InetSocketAddress unreachableDestSequenceNumber;


    public RERR() {

    }
}
