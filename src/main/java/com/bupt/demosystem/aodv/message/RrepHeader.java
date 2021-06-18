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
    private byte m_flags;

    /**
     * Prefix Size
     */
    private byte m_prefixSize;

    /**
     * 从源节点到目的节点经过的节点数
     */
    private int m_hopCount;


    /**
     * 目标接收ip
     */
    private InetSocketAddress m_dst;

    /**
     * The destination sequence number associated to the route.
     */
    private int m_dstSeqNo;

    /**
     * rreq发起者ip地址
     */
    private InetSocketAddress m_origin;

    /**
     * 节点接收的时间（ms），rrep认为路由是有效的，节点只有在这段时间前收到该条RREP信息，才承认RREP信息有效，否则则视为无效
     */
    private int m_lifeTime;

    public RrepHeader(byte m_prefixSize, int m_hopCount, InetSocketAddress m_dst, int m_dstSeqNo, InetSocketAddress m_origin, int m_lifeTime) {
        this.m_flags = 0;
        this.m_prefixSize = m_prefixSize;
        this.m_hopCount = m_hopCount;
        this.m_dst = m_dst;
        this.m_dstSeqNo = m_dstSeqNo;
        this.m_origin = m_origin;
        this.m_lifeTime = m_lifeTime;
    }


    public void setHello(InetSocketAddress origin, int srcSeqNo, int lifeTime) {
        this.m_flags = 0;
        this.m_prefixSize = 0;
        this.m_hopCount = 0;
        this.m_dst = origin;
        this.m_dstSeqNo = srcSeqNo;
        this.m_origin = origin;
        this.m_lifeTime = lifeTime;
    }

    public void setAckRequired(boolean f) {
        if (f) {
            this.m_flags |= (1 << 6);
        } else {
            this.m_flags &= ~(1 << 6);
        }
    }

    public boolean getAckRequired() {
        return (this.m_flags & (1 << 6)) > 0;
    }

    public byte getM_flags() {
        return m_flags;
    }

    public void setM_flags(byte m_flags) {
        this.m_flags = m_flags;
    }

    public byte getM_prefixSize() {
        return m_prefixSize;
    }

    public void setM_prefixSize(byte m_prefixSize) {
        this.m_prefixSize = m_prefixSize;
    }

    public int getM_hopCount() {
        return m_hopCount;
    }

    public void setM_hopCount(int m_hopCount) {
        this.m_hopCount = m_hopCount;
    }

    public InetSocketAddress getM_dst() {
        return m_dst;
    }

    public void setM_dst(InetSocketAddress m_dst) {
        this.m_dst = m_dst;
    }

    public int getM_dstSeqNo() {
        return m_dstSeqNo;
    }

    public void setM_dstSeqNo(int m_dstSeqNo) {
        this.m_dstSeqNo = m_dstSeqNo;
    }

    public InetSocketAddress getM_origin() {
        return m_origin;
    }

    public void setM_origin(InetSocketAddress m_origin) {
        this.m_origin = m_origin;
    }

    public int getM_lifeTime() {
        return m_lifeTime;
    }

    public void setM_lifeTime(int m_lifeTime) {
        this.m_lifeTime = m_lifeTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RrepHeader that = (RrepHeader) o;
        return m_flags == that.m_flags && m_prefixSize == that.m_prefixSize && m_hopCount == that.m_hopCount && m_dstSeqNo == that.m_dstSeqNo && m_lifeTime == that.m_lifeTime && Objects.equals(m_dst, that.m_dst) && Objects.equals(m_origin, that.m_origin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_flags, m_prefixSize, m_hopCount, m_dst, m_dstSeqNo, m_origin, m_lifeTime);
    }
}
