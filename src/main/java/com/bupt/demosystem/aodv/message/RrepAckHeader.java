package com.bupt.demosystem.aodv.message;

/**
 * @Author banbridge
 * @Classname RREP_ACK
 * @Date 2021/6/3 17:22
 * RREP-ACK消息格式用于回复标志位A设为1的RREP消息。
 * 这经常发生于节点怀疑链路不可靠或者只能单向传播，
 * RREP-ACK意义在于告知发送RREP的节点目的节点已经
 * 收到RREP消息，并且暗示了链路是双向传播和可靠的。
0                   1
0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|     Type      |   Reserved    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class RrepAckHeader {

    private int reserved;

    public RrepAckHeader(int reserved) {
        this.reserved = reserved;
    }

    public RrepAckHeader() {
        this.reserved = 0;
    }
}
