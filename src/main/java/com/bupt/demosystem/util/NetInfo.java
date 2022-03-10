package com.bupt.demosystem.util;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.entity.Node;
import com.bupt.demosystem.service.NetCreateService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Banbridge on 2021/2/21.
 * 操作所有群的消息
 */
@Service
public class NetInfo {

    private final int SIZEOFNET = 3;

    private int selectIndex;

    private int count;

    //得到预估路径的
    private int index_path_data;

    private ArrayList<ArrayList<double[]>> pathData;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }


    //簇的存储
    private ArrayList<Network> nets;

    // 每个簇的业务统计情况
    private ArrayList<CountTransmit> countTransmits;
    String ipBase = "192.168.";

    public NetInfo() {
        count = 1;
        nets = new ArrayList<>();
        initNetInfo();
    }

    /**
     * 进行一些初始化操作，或者重新请求仿真时候
     */
    public void initNetInfo() {
        selectIndex = 0;
        index_path_data = 0;
        if (nets.size() < 1) {
            NetCreateService netCreateService = new NetCreateService();
            Network net1 = netCreateService.getNetwork(18, ipBase + "1.");
            nets.add(net1);
            Network net2 = netCreateService.getNetwork(20, ipBase + "2.");
            nets.add(net2);
            Network net3 = netCreateService.getNetwork(15, ipBase + "3.");
            nets.add(net3);
        }
        getPathData();
        for (int i = 0; i < nets.size(); i++) {
            List<Node> nodes = nets.get(i).getNodeList();
            double[] poi = pathData.get(i).get(index_path_data);
            for (Node node : nodes) {
                node.setLatitude(poi[1] + (node.getX() - 50) * 1.0 / 100 * 0.05);
                node.setLongitude(poi[0] + (node.getY() - 50) * 1.0 / 100 * 0.05);
                node.setHeight(poi[2]);
            }
        }
        index_path_data++;
    }

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
            initNetInfo();
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
    public boolean destroyNode(int c_id, int n_id) {
        if (c_id < nets.size()) {
            Network net = nets.get(c_id);
            return net.destroy(n_id);
        }
        return false;
    }


    public synchronized boolean moveNodeCluster() {
        if (!(index_path_data < pathData.get(0).size())) {
            return false;
        }
        for (int i = 0; i < nets.size(); i++) {
            Network net = nets.get(i);
            List<Node> nodeList = net.getNodeList();
            double[] location = pathData.get(i).get(index_path_data);
            double[] pre_location = pathData.get(i).get(index_path_data - 1);
            double lat, lon, hei;
            for (Node node : nodeList) {
                if (node.getType() != -1) {
                    lon = node.getLongitude() + location[0] - pre_location[0] + (Math.random() - 0.5) * 0.005;
                    lat = node.getLatitude() + location[1] - pre_location[1] + (Math.random() - 0.5) * 0.005;
                    hei = node.getHeight() + location[2] - pre_location[2] + (Math.random() - 0.5) * 100;
                    node.setLatitude(lat);
                    node.setLongitude(lon);
                    node.setHeight(hei);
                    if (nodeList.get(net.getClusterId()).getType() == -1 || nodeList.get(net.getClusterId()).getEdges().size() < node.getEdges().size()) {
                        net.setClusterId(node.getId());
                    }
                    node.setType(1);
                }

            }
            net.getNodeList().get(net.getClusterId()).setType(0);
        }
        index_path_data++;
        return true;
    }


    public ArrayList<ArrayList<double[]>> getPathData() {
        if (pathData == null) {
            setPathData(null);
        }
        return pathData;
    }


    /**
     * 计算输入最大高度，三个群的起始位置，三个群的终点位置，计算预估路径
     *
     * @param pathData
     */
    public void setPathData(ArrayList<ArrayList<double[]>> pathData) {
        int len = nets.size();
        int totalSeconds = 60 * 5;
        int[] maxHeight = {50000, 20000, 5000};
        double[][] start = new double[len][3];
        start[0] = new double[]{119.78, 26.38, 100};
        start[1] = new double[]{120.78, 26.0, 100};
        start[2] = new double[]{121.50, 25.64, 100};
        double[] end = {123.77, 24.34, 100};
        ArrayList<ArrayList<double[]>> data = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            ArrayList<double[]> c_i = new ArrayList<>(totalSeconds);
            c_i.add(start[i]);
            double each_step_jing = (end[0] - start[i][0]) / totalSeconds;
            double each_step_wei = (end[1] - start[i][1]) / totalSeconds;
            int mid = totalSeconds / 2;
            for (int j = 1; j < mid; j++) {
                double[] now = c_i.get(j - 1);
                c_i.add(new double[]{now[0] + each_step_jing, now[1] + each_step_wei, maxHeight[i] * getParabola(j, mid)});
            }
            for (int j = mid; j < totalSeconds; j++) {
                double[] now = c_i.get(j - 1);
                c_i.add(new double[]{now[0] + each_step_jing, now[1] + each_step_wei, maxHeight[i] * getParabola(j, mid)});
            }
            data.add(c_i);
        }
        this.pathData = data;
    }

    public double getParabola(int index, int len) {
        int fenzi = index - len;
        int fenmu = len;
        double ans = 1 - fenzi * fenzi * 1.0 / (fenmu * fenmu);
        return ans;
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
