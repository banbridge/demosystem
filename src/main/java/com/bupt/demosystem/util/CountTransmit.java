package com.bupt.demosystem.util;


import java.io.Serializable;
import java.util.*;

/**
 * @Author banbridge
 * @Classname CountTransmit
 * @Date 2022/3/13 13:15
 */
public class CountTransmit implements Serializable {


    private int[] sendSum;
    private int[] recvSum;
    private Map<Integer, Integer> recv;
    private Map<Integer, Integer> send;
    private int destroyNode;
    private int totalNode;
    private int size;

    public CountTransmit(int size) {
        this.size = size;
        send = new HashMap<>();
        recv = new HashMap<>();

        sendSum = new int[size];
        recvSum = new int[size];
        this.destroyNode = 0;
    }

    public void increaseSend(int start, int index) {
        sendSum[index]++;
        sendSum[0]++;
        int cnt = send.getOrDefault(start, 0);
        send.put(start, cnt + 1);
    }

    public void increaseRecv(int start, int index) {
        recvSum[index]++;
        recvSum[0]++;
        int cnt = recv.getOrDefault(start, 0);
        recv.put(start, cnt + 1);
    }


    public int[] getSendSum() {
        return sendSum;
    }

    public int[] getRecvSum() {
        return recvSum;
    }

    public Map<Integer, Integer> getRecv() {
        return recv;
    }

    public Map<Integer, Integer> getSend() {
        return send;
    }

    public int getDestroyNode() {
        return destroyNode;
    }

    public void setDestroyNode(int destroyNode) {
        this.destroyNode = destroyNode;
    }

    public int getTotalNode() {
        return totalNode;
    }

    public void setTotalNode(int totalNode) {
        this.totalNode = totalNode;
    }

    public int getRate() {
        return destroyNode * 100 / totalNode;
    }

    public boolean addCountTransmit(CountTransmit countTransmit) {
        if (countTransmit == null || this.size != countTransmit.size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            recvSum[i] += countTransmit.getRecvSum()[i];
            sendSum[i] += countTransmit.getSendSum()[i];
        }
        return true;
    }

    public boolean divideCountTransmit(int n) {
        if (n == 0) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            recvSum[i] /= n;
            sendSum[i] /= n;
        }
        return true;
    }

}
