package com.bupt.demosystem.aodv.message;

import com.bupt.demosystem.aodv.message.tool.RouterFlags;

import java.net.InetSocketAddress;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author banbridge
 * @Classname RouterTable
 * @Date 2021/6/2 16:42
 */
public class RoutingTableEntry {

    /**
     * 目标ip地址,可以讲InetSocketAddress转化为String类型
     */
    private InetSocketAddress destIpAddress;


    /**
     * 下一跳地址
     */
    private InetSocketAddress nextHop;

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
    private int hops;

    /**
     * 有效的目标序列号标志
     */
    private boolean validSeqNo;

    /**
     * \brief Expiration or deletion time of the route
     * Lifetime field in the routing table plays dual role:
     * for an active route it is the expiration time, and for an invalid route
     * it is the deletion time.
     */
    private LocalTime lifeTime;

    /**
     * 其他状态和路由标志
     */
    private int otherFlag;

    /**
     * Routing flags: valid, invalid, or in_search
     */
    private RouterFlags flag;

    /**
     * 上游节点列表， 用于建立反向路由，当下游链路出现问题时可以通知上游节点
     */
    private LinkedList<InetSocketAddress> precursorsList;

    /**
     * when I can send another requests
     */
    private LocalTime routeRequestTimeout;

    /**
     * Number of route requests
     */
    private int reqCount;

    /**
     * Indicate if this entry is in "blacklist"
     */
    private boolean blackListState;

    /**
     * Time for which the node is put into the blacklist
     */
    private LocalTime blackListTimeout;


    public RoutingTableEntry() {
    }

    public RoutingTableEntry(InetSocketAddress destIpAddress, InetSocketAddress nextHop, int destSequenceNumber, int hops, boolean validSeqNo, int lifeTime_, LocalTime blackListTimeout) {
        this.destIpAddress = destIpAddress;
        this.nextHop = nextHop;
        this.destSequenceNumber = destSequenceNumber;
        this.hops = hops;
        this.validSeqNo = validSeqNo;
        this.lifeTime = LocalTime.now().plus(lifeTime_, ChronoUnit.MILLIS);
        this.flag = RouterFlags.VALID;
        this.reqCount = 0;
        this.blackListState = false;
        this.blackListTimeout = blackListTimeout;
        this.precursorsList = new LinkedList<>();
    }

    //name Precursors management

    /**
     * Insert precursor in precursor list if it doesn't yet exist in the list
     * \param id precursor address
     * \return true on success
     */
    public boolean insertPrecursor(InetSocketAddress id) {
        return precursorsList.add(id);
    }

    /**
     * Lookup precursor by address
     * \param id precursor address
     * \return true on success
     */
    public boolean lookupPrecursor(InetSocketAddress id) {
        return this.precursorsList.contains(id);
    }

    /**
     * \brief Delete precursor
     * \param id precursor address
     * \return true on success
     */
    public boolean deletePrecursor(InetSocketAddress id) {
        return precursorsList.remove(id);
    }

    /**
     * Delete all precursors
     */
    public void deleteAllPrecursors() {
        precursorsList.clear();
    }

    /**
     * Check that precursor list is empty
     * \return true if precursor list is empty
     */
    public boolean isPrecursorListEmpty() {
        return precursorsList.isEmpty();
    }

    /**
     * Inserts precursors in output parameter prec if they do not yet exist in vector
     * \param prec vector of precursor addresses
     */
    public void getPrecursors() {
        if (isPrecursorListEmpty()) {
            return;
        }
        for (InetSocketAddress id : precursorsList) {

        }
    }

    ;
    //\}

    /**
     * Mark entry as "down" (i.e. disable it)
     * \param badLinkLifetime duration to keep entry marked as invalid
     */
    public void Invalidate(int badLinkLifetime) {
        if (flag == RouterFlags.INVALID) {
            return;
        }
        flag = RouterFlags.INVALID;
        reqCount = 0;
        lifeTime = LocalTime.now().plus(badLinkLifetime, ChronoUnit.MILLIS);
    }


    public InetSocketAddress getDestIpAddress() {
        return destIpAddress;
    }

    public void setDestIpAddress(InetSocketAddress destIpAddress) {
        this.destIpAddress = destIpAddress;
    }

    public InetSocketAddress getNextHop() {
        return nextHop;
    }

    public void setNextHop(InetSocketAddress nextHop) {
        this.nextHop = nextHop;
    }

    public int getDestSequenceNumber() {
        return destSequenceNumber;
    }

    public void setDestSequenceNumber(int destSequenceNumber) {
        this.destSequenceNumber = destSequenceNumber;
    }

    public int getHops() {
        return hops;
    }

    public void setHops(int hops) {
        this.hops = hops;
    }

    public boolean isValidSeqNo() {
        return validSeqNo;
    }

    public void setValidSeqNo(boolean validSeqNo) {
        this.validSeqNo = validSeqNo;
    }

    public LocalTime getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(LocalTime lifeTime) {
        this.lifeTime = lifeTime;
    }

    public int getOtherFlag() {
        return otherFlag;
    }

    public void setOtherFlag(int otherFlag) {
        this.otherFlag = otherFlag;
    }

    public RouterFlags getFlag() {
        return flag;
    }

    public void setFlag(RouterFlags flag) {
        this.flag = flag;
    }


    public LocalTime getRouteRequestTimeout() {
        return routeRequestTimeout;
    }

    public void setRouteRequestTimeout(LocalTime routeRequestTimeout) {
        this.routeRequestTimeout = routeRequestTimeout;
    }

    public int getReqCount() {
        return reqCount;
    }

    public void setReqCount(int reqCount) {
        this.reqCount = reqCount;
    }

    public boolean isBlackListState() {
        return blackListState;
    }

    public void setBlackListState(boolean blackListState) {
        this.blackListState = blackListState;
    }

    public LocalTime getBlackListTimeout() {
        return blackListTimeout;
    }

    public void setBlackListTimeout(LocalTime blackListTimeout) {
        this.blackListTimeout = blackListTimeout;
    }
}
