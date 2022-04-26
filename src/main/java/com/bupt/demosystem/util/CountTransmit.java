package com.bupt.demosystem.util;

import com.bupt.demosystem.entity.Network;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author banbridge
 * @Classname CountTransmit
 * @Date 2022/3/13 13:15
 */
public class CountTransmit {

    private int send[];
    private int recv[];
    private ArrayList<int[]> sendNode;
    private ArrayList<int[]> recvNode;
    private int sendSum;
    private int recvSum;

    public CountTransmit(List<Network> nets) {
        this.send = new int[nets.size()];
        this.recv = new int[nets.size()];
        sendNode = new ArrayList<>(nets.size());
        recvNode = new ArrayList<>(nets.size());
        for (int i = 0; i < nets.size(); i++) {
            int[] cnt = new int[nets.get(i).getNodeList().size()];
            sendNode.add(cnt);
        }
        for (int i = 0; i < nets.size(); i++) {
            int[] cnt = new int[nets.get(i).getNodeList().size()];
            recvNode.add(cnt);
        }
        sendSum = 0;
        recvSum = 0;

    }

    public void increaseSend(int c_id, int n_id) {
        send[c_id]++;
        sendNode.get(c_id)[n_id]++;
        sendSum++;
    }

    public void increaseRecv(int c_id, int n_id) {
        recv[c_id]++;
        recvNode.get(c_id)[n_id]++;
        recvSum++;
    }

    public ArrayList<int[]> getSendNode() {
        return sendNode;
    }

    public void setSendNode(ArrayList<int[]> sendNode) {
        this.sendNode = sendNode;
    }

    public ArrayList<int[]> getRecvNode() {
        return recvNode;
    }

    public void setRecvNode(ArrayList<int[]> recvNode) {
        this.recvNode = recvNode;
    }

    public int[] getSend() {
        return send;
    }

    public int[] getRecv() {
        return recv;
    }

    public int getSendSum() {
        return sendSum;
    }

    public int getRecvSum() {
        return recvSum;
    }
}
