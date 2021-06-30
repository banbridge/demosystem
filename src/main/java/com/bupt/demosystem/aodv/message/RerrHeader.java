package com.bupt.demosystem.aodv.message;

import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * @author banbridge
 * 当链路发生故障导致一个或者多个目的节点不可达时，RERR消息就会被发送，
 * 设计RERR消息是为了能通知网络中其他节点哪些目的节点因为故障导致不可访问，
0                   1                   2                   3
0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|     Type      |N|          Reserved           |   DestCount   |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|            Unreachable Destination IP Address (1)             |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|         Unreachable Destination Sequence Number (1)           |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-|
|  Additional Unreachable Destination IP Addresses (if needed)  |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|Additional Unreachable Destination Sequence Numbers (if needed)|
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class RerrHeader {

    private byte flags;

    private byte reserved;

    private HashMap<InetSocketAddress, Integer> unreachableDstSeqNo;

    public RerrHeader() {
        this.flags = 0;
        this.reserved = 0;
        this.unreachableDstSeqNo = new HashMap<>();
    }


    // No delete flag

    /**
     * \brief Set the no delete flag
     * \param f the no delete flag
     */
    public void SetNoDelete(boolean f) {
        if (f) {
            this.flags |= (1 << 0);
        } else {
            this.flags &= ~(1 << 0);
        }
    }

    /**
     * \brief Get the no delete flag
     * \return the no delete flag
     */
    public boolean GetNoDelete() {
        return (this.flags & (1 << 0)) > 0;
    }

    /**
     * \brief Add unreachable node address and its sequence number in RERR header
     * \param dst unreachable IPv4 address
     * \param seqNo unreachable sequence number
     * \return false if we already added maximum possible number of unreachable destinations
     */
    public boolean AddUnDestination(InetSocketAddress dst, int seqNo) {
        if (unreachableDstSeqNo.containsKey(dst)) {
            return true;
        }
        assert GetDestCount() < 255;
        unreachableDstSeqNo.put(dst, seqNo);
        return true;
    }

    /**
     * \brief Delete pair (address + sequence number) from REER header, if the number of unreachable destinations > 0
     * \param un unreachable pair (address + sequence number)
     * \return true on success
     */
    public boolean RemoveUnDestination(InetSocketAddress dst) {
        if (unreachableDstSeqNo.isEmpty()) {
            return false;
        }
        return unreachableDstSeqNo.remove(dst) == null;
    }

    /// Clear header
    void Clear() {
        flags = 0;
        reserved = 0;
        unreachableDstSeqNo.clear();
    }

    /**
     * \returns number of unreachable destinations in RERR message
     */
    public int GetDestCount() {
        return unreachableDstSeqNo.size();
    }

}
