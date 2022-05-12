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

    public CountTransmit(int size) {
        send = new HashMap<>();
        recv = new HashMap<>();

        sendSum = new int[size];
        recvSum = new int[size];

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
}
