package com.bupt.demosystem.aodv.message;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * @author banbridge
 * 当节点需要与某个目的节点传输数据，但没有目的节点的合法路由，可以向全网广播RREQ消息，向网络寻求到
 * 目的节点的路由，并且在约定的时间内等待携带有到目的节点路由信息的RREP消息回来，若规定时间内无收到
 * RREP回复，则重发RREQ消息，直到达到最大发送次数。其他节点根据收到RREQ消息的接口建立从当前节点到
 * 源节点的反向路由。
 */
public class RREQ {


    /**
     * Destination only flag; indicates only the destination may respond to this RREQ
     */
    private boolean flagD;

    /**
     * Unknown sequence number; indicates the destination sequence number is unknown
     */
    private boolean flagU;

    /**
     * 从源节点到收到RREQ消息节点所经过的跳数
     */
    private int hopCount;

    /**
     * 路由请求消息表示，rrqId和发起节点的IP地址可以作为RREQ的消息的唯一标识
     */
    private int rreqId;

    /**
     * 需要路由的目的地址
     */
    private InetSocketAddress destIpAddress;

    /**
     * 是源节点路由表中存储的关于目的节点的最新序列号，其序列号可能不是最新的序列号，也可能不存在，如果源节点没有目的节点序列号，则把标志为U设为1， 此时该字段无意义
     * The latest sequence number received in the past by the originator for any route towards the destination.
     */
    private int destSequenceNumber;

    /**
     * rreq发起者的ip地址
     */
    private InetSocketAddress origIpAddress;

    /**
     * 由源节点自身维护，每次更新序列号，如果超过了系统最大值，重新设置为系统最下值
     * The current sequence number to be used in the route entry pointing towards the originator of the route request.
     */
    private int origSequenceNumber;

    public boolean isFlagD() {
        return flagD;
    }

    public void setFlagD(boolean flagD) {
        this.flagD = flagD;
    }

    public boolean isFlagU() {
        return flagU;
    }

    public void setFlagU(boolean flagU) {
        this.flagU = flagU;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public int getRreqId() {
        return rreqId;
    }

    public void setRreqId(int rreqId) {
        this.rreqId = rreqId;
    }

    public InetSocketAddress getDestIpAddress() {
        return destIpAddress;
    }

    public void setDestIpAddress(InetSocketAddress destIpAddress) {
        this.destIpAddress = destIpAddress;
    }

    public int getDestSequenceNumber() {
        return destSequenceNumber;
    }

    public void setDestSequenceNumber(int destSequenceNumber) {
        this.destSequenceNumber = destSequenceNumber;
    }

    public InetSocketAddress getOrigIpAddress() {
        return origIpAddress;
    }

    public void setOrigIpAddress(InetSocketAddress origIpAddress) {
        this.origIpAddress = origIpAddress;
    }

    public int getOrigSequenceNumber() {
        return origSequenceNumber;
    }

    public void setOrigSequenceNumber(int origSequenceNumber) {
        this.origSequenceNumber = origSequenceNumber;
    }
}
