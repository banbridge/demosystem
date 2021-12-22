package com.bupt.demosystem.util;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.entity.Node;
import com.bupt.demosystem.service.NetCreateService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Banbridge on 2021/2/21.
 */
@Service
public class NetInfo {

    private final int SIZEOFNET = 3;

    private static int selectIndex = 0;

    private int count = 1;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    //簇的存储
    private ArrayList<Network> nets = new ArrayList<>();
    //每个簇的邻接矩阵存储方式
    private ArrayList<int[][]> maps = new ArrayList<>();
    // 每个簇的业务统计情况
    private ArrayList<CountTransmit> countTransmits = new ArrayList<>();
    String ipBase = "192.168.";


    /**
     * 得到第一簇的net
     *
     * @return
     */
    public Network getNet() {
        return nets.get(0);
    }

    public void setSelectIndex(int index) {
        selectIndex = index;
    }

    public Network getSelectNetIndex() {
        return nets.get(selectIndex);
    }

    public void setSelectNet(Network net) {
        for (Node node : net.getNodeList()) {
            node.setIp(String.format("%s%d.%d", ipBase, selectIndex + 1, node.getId()));
        }

        nets.set(selectIndex, net);
    }

    /**
     * 得到所有群的信息
     *
     * @return
     */
    public List<Network> getAllNet() {
        if (nets.size() < 1) {
            selectIndex = 0;
            NetCreateService netCreateService = new NetCreateService();
            Network net1 = netCreateService.getNetwork(18, ipBase + "1.");
            nets.add(net1);
            Network net2 = netCreateService.getNetwork(20, ipBase + "2.");
            nets.add(net2);
            Network net3 = netCreateService.getNetwork(15, ipBase + "3.");
            nets.add(net3);
        }
        return nets;
    }

    /**
     * 根据选中的序号来得到群的信息
     *
     * @param index
     * @return
     */
    public Network getNetByIndex(Integer index) {
        if (index < nets.size()) {
            selectIndex = index;
            return nets.get(index);
        }
        return null;
    }

    /**
     * 根据簇的id和节点id毁坏节点
     *
     * @param c_id
     * @param n_id
     */
    public void destroyNode(int c_id, int n_id) {

    }


}


class CountTransmit {
    private int send[];
    private int recv[];
    private int sendSum;
    private int recvSum;

    public CountTransmit(int n) {
        this.send = new int[n];
        this.recv = new int[n];
        sendSum = 0;
        recvSum = 0;
    }

    public void increaseSend(int index) {
        send[index]++;
        sendSum++;
    }

    public void increaseRecv(int index) {
        recv[index]++;
        recvSum++;
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
