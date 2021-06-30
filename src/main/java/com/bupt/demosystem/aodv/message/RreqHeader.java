package com.bupt.demosystem.aodv.message;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;

/**
 * @author banbridge
 * 当节点需要与某个目的节点传输数据，但没有目的节点的合法路由，可以向全网广播RREQ消息，向网络寻求到
 * 目的节点的路由，并且在约定的时间内等待携带有到目的节点路由信息的RREP消息回来，若规定时间内无收到
 * RREP回复，则重发RREQ消息，直到达到最大发送次数。其他节点根据收到RREQ消息的接口建立从当前节点到
 * 源节点的反向路由。
0                   1                   2                   3
0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|     Type      |J|R|G|D|U|   Reserved          |   Hop Count   |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                            RREQ ID                            |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                    Destination IP Address                     |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                  Destination Sequence Number                  |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                    Originator IP Address                      |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                  Originator Sequence Number                   |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class RreqHeader {

    /**
     * |J|R|G|D|U| bit flags
     */
    private byte flags;

    /**
     * not used (must be 0)
     */
    private byte reserved;

    /**
     * 从源节点到收到RREQ消息节点所经过的跳数
     */
    private int hopCount;

    /**
     * 路由请求消息表示，rrqId和发起节点的IP地址可以作为RREQ的消息的唯一标识
     */
    private int requestID;

    /**
     * 需要路由的目的地址
     */
    private InetSocketAddress dst;

    /**
     * 是源节点路由表中存储的关于目的节点的最新序列号，其序列号可能不是最新的序列号，也可能不存在，如果源节点没有目的节点序列号，则把标志为U设为1， 此时该字段无意义
     * The latest sequence number received in the past by the originator for any route towards the destination.
     */
    private int dstSeqNo;

    /**
     * rreq发起者的ip地址
     */
    private InetSocketAddress origin;

    /**
     * 由源节点自身维护，每次更新序列号，如果超过了系统最大值，重新设置为系统最下值
     * The current sequence number to be used in the route entry pointing towards the originator of the route request.
     */
    private int originSeqNo;

    public RreqHeader() {
        this.flags = 0;
        this.reserved = 0;
        this.hopCount = 0;
        this.requestID = 0;
        this.dstSeqNo = 0;
        this.originSeqNo = 0;

    }

    public RreqHeader(InetSocketAddress dst, InetSocketAddress origin) {
        this.flags = 0;
        this.reserved = 0;
        this.hopCount = 0;
        this.requestID = 0;
        this.dstSeqNo = 0;
        this.originSeqNo = 0;
        this.dst = dst;
        this.origin = origin;
    }

    // Flags

    /**
     * \brief Set the gratuitous RREP flag
     * \param f the gratuitous RREP flag
     */
    public void SetGratuitousRrep(boolean f) {
        if (f) {
            this.flags |= (1 << 5);
        } else {
            this.flags &= ~(1 << 5);
        }
    }

    /**
     * \brief Get the gratuitous RREP flag
     * \return the gratuitous RREP flag
     */
    public boolean GetGratuitousRrep() {
        return (this.flags & (1 << 5)) > 0;
    }

    /**
     * \brief Set the Destination only flag
     * \param f the Destination only flag
     */
    public void SetDestinationOnly(boolean f) {
        if (f) {
            this.flags |= (1 << 4);
        } else {
            this.flags &= ~(1 << 4);
        }
    }

    /**
     * \brief Get the Destination only flag
     * \return the Destination only flag
     */
    public boolean GetDestinationOnly() {
        return (this.flags & (1 << 4)) > 0;
    }

    /**
     * \brief Set the unknown sequence number flag
     * \param f the unknown sequence number flag
     */
    public void SetUnknownSeqno(boolean f) {
        if (f) {
            this.flags |= (1 << 3);
        } else {
            this.flags &= ~(1 << 3);
        }
    }

    /**
     * \brief Get the unknown sequence number flag
     * \return the unknown sequence number flag
     */
    public boolean GetUnknownSeqno() {
        return (this.flags & (1 << 3)) > 0;
    }


    public byte getFlags() {
        return flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public byte getReserved() {
        return reserved;
    }

    public void setReserved(byte reserved) {
        this.reserved = reserved;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
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

    public int getOriginSeqNo() {
        return originSeqNo;
    }

    public void setOriginSeqNo(int originSeqNo) {
        this.originSeqNo = originSeqNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RreqHeader that = (RreqHeader) o;
        return flags == that.flags && reserved == that.reserved && hopCount == that.hopCount && requestID == that.requestID && dstSeqNo == that.dstSeqNo && originSeqNo == that.originSeqNo && dst.equals(that.dst) && origin.equals(that.origin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flags, reserved, hopCount, requestID, dst, dstSeqNo, origin, originSeqNo);
    }
}
