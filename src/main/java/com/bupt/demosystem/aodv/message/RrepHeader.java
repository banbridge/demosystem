package com.bupt.demosystem.aodv.message;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author banbridge
 * RREP消息用于单播回复RREQ消息，目的是为了告知发送RREQ消息的源节点到目
 * 的节点的路由。通过RREP消息可以建立从收到RREP消息的节点到RREP消息中的
 * 目的节点的正向路由，用于以后发送数据到目的节点。
0                   1                   2                   3
0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|     Type      |R|A|    Reserved     |Prefix Sz|   Hop Count   |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                     Destination IP address                    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                  Destination Sequence Number                  |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                    Originator IP address                      |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                           Lifetime                            |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class RrepHeader {


    /*
    Hello消息用于活跃节点向所有邻近节点广播自身的存在，当一个节点处于正在使用的路由中时，需要定时向邻近节点广播Hello消息，若邻近节点收到Hello消息，则更新路由表中对应节点的生存时间。
    Hello消息是一种特殊的RREP消息，其特殊之处在于为某些字段设置了特殊值
    目的IP地址      设置为发送节点自身的IP地址
    目的序列号      设置为发送节点自身最新的序列号
    跳数           设置为0
    生存时间        设置为ALLOW_HELLO_LOSS * HELLO_INTERVAL
        */

    /**
     * A - acknowledgment required flag
     */
    private byte flags;

    /**
     * Prefix Size
     */
    private byte prefixSize;

    /**
     * 从源节点到目的节点经过的节点数
     */
    private int hopCount;


    /**
     * 目标接收ip
     */
    private InetSocketAddress dst;

    /**
     * The destination sequence number associated to the route.
     */
    private int dstSeqNo;

    /**
     * rreq发起者ip地址
     */
    private InetSocketAddress origin;

    /**
     * 节点接收的时间（ms），rrep认为路由是有效的，节点只有在这段时间前收到该条RREP信息，才承认RREP信息有效，否则则视为无效
     */
    private int lifeTime;

    public RrepHeader(byte prefixSize, int hopCount, InetSocketAddress dst, int dstSeqNo, InetSocketAddress origin, int lifeTime) {
        this.flags = 0;
        this.prefixSize = prefixSize;
        this.hopCount = hopCount;
        this.dst = dst;
        this.dstSeqNo = dstSeqNo;
        this.origin = origin;
        this.lifeTime = lifeTime;
    }


    public void setHello(InetSocketAddress origin, int srcSeqNo, int lifeTime) {
        this.flags = 0;
        this.prefixSize = 0;
        this.hopCount = 0;
        this.dst = origin;
        this.dstSeqNo = srcSeqNo;
        this.origin = origin;
        this.lifeTime = lifeTime;
    }

    public void setAckRequired(boolean f) {
        if (f) {
            this.flags |= (1 << 6);
        } else {
            this.flags &= ~(1 << 6);
        }
    }

    public boolean getAckRequired() {
        return (this.flags & (1 << 6)) > 0;
    }

    public byte getFlags() {
        return flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public byte getPrefixSize() {
        return prefixSize;
    }

    public void setPrefixSize(byte prefixSize) {
        this.prefixSize = prefixSize;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public InetSocketAddress getDst() {
        return dst;
    }

    public void setDst(InetSocketAddress dst) {
        this.dst = dst;
    }

    public int getDstSeqNo() {
        return dstSeqNo;
    }

    public void setDstSeqNo(int dstSeqNo) {
        this.dstSeqNo = dstSeqNo;
    }

    public InetSocketAddress getOrigin() {
        return origin;
    }

    public void setOrigin(InetSocketAddress origin) {
        this.origin = origin;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RrepHeader that = (RrepHeader) o;
        return flags == that.flags && prefixSize == that.prefixSize && hopCount == that.hopCount && dstSeqNo == that.dstSeqNo && lifeTime == that.lifeTime && Objects.equals(dst, that.dst) && Objects.equals(origin, that.origin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flags, prefixSize, hopCount, dst, dstSeqNo, origin, lifeTime);
    }
}
