package com.bupt.demosystem.util;

import com.bupt.demosystem.entity.Cluster;
import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.entity.Node;
import com.bupt.demosystem.service.NetCreateService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author banbridge
 * @Classname Group
 * @Date 2022/4/26 10:09
 */

@Service
public class Group {

    private final int SIZEOFNET = 3;

    private int selectIndex;

    //三个群的路径索引
    private int[] clusterPathIndex;
    String ipBase = "192.168.";


    private ArrayList<ArrayList<double[]>> pathData;

    private List<CountTransmit> countTransmit;
    private Map<Integer, Network> clusterToGroup;


    //簇的存储
    private ArrayList<Cluster> groups;
    private ArrayList<Network> allNets;


    public Group() {
        groups = new ArrayList<>(5);
        clusterToGroup = new HashMap<>();
        allNets = new ArrayList<>();
        initNetInfo();
    }

    /**
     * 进行一些初始化操作，或者重新请求仿真时候
     */
    public void initNetInfo() {
        selectIndex = 0;

        if (groups.size() < 1) {

            NetCreateService netCreateService = new NetCreateService();
            int[] nodeSize = {24, 24, 80};
            for (int i = 1; i <= 3; i++) {
                ArrayList<Network> nets = netCreateService.getNetworkList(nodeSize[i], ipBase + i + ".", i);
                Cluster cls = new Cluster();
                cls.setCluster(nets);
                groups.add(cls);
            }
            updateClusterToGroup();

//            Network net1 = netCreateService.getNetwork(24, ipBase + "0.", 1);
//            nets.add(net1);
//            Network net2 = netCreateService.getNetwork(24, ipBase + "1.", 2);
//            nets.add(net2);
//            Network net3 = netCreateService.getNetwork(20, ipBase + "2.", 3);
//            nets.add(net3);
        }
        clusterPathIndex = new int[groups.size()];
        getPathData();
        for (int i = 0; i < groups.size(); i++) {
            for (Network network : groups.get(i).getCluster()) {
                List<Node> nodes = network.getNodeList();
                double[] poi = pathData.get(i).get(clusterPathIndex[i]);
                for (Node node : nodes) {
                    node.setLatitude(poi[1] + (node.getX() - 50) * 1.0 / 100 * 0.09);
                    node.setLongitude(poi[0] + (node.getY() - 50) * 1.0 / 100 * 0.09);
                    node.setHeight(poi[2]);
                    node.setType(1);
                }
                nodes.get(0).setType(0);
            }

            clusterPathIndex[i]++;
        }
        countTransmit = new ArrayList<>();
        for (int i = 0; i < allNets.size(); i++) {
            countTransmit.add(new CountTransmit(allNets));
        }
    }

    /**
     * 得到第一簇的net
     *
     * @return
     */
    public Cluster getNet() {
        return groups.get(0);
    }

    public void setSelectIndex(int index) {
        selectIndex = index;
    }

    public Cluster getSelectNetIndex() {
        return groups.get(selectIndex);
    }

    public void setSelectNet(Cluster cls) {
        for (Network network : cls.getCluster()) {
            for (Node node : network.getNodeList()) {
                node.setIp(String.format("%s%d.%d", ipBase, selectIndex + 1, node.getId()));
            }
        }
        updateClusterToGroup();
        groups.set(selectIndex, cls);
    }

    private void updateClusterToGroup() {
        clusterToGroup.clear();
        allNets.clear();
        int cnt = 0;
        for (int i = 0; i < groups.size(); i++) {
            Cluster nets = groups.get(i);
            for (Network net : nets.getCluster()) {
                clusterToGroup.put(cnt++, net);
                allNets.add(net);
            }
        }
    }


    public List<CountTransmit> getCountTransmit() {
        return countTransmit;
    }

    public void setCountTransmit(List<CountTransmit> countTransmit) {
        this.countTransmit = countTransmit;
    }

    /**
     * 得到所有群的信息
     *
     * @return
     */
    public ArrayList<Cluster> getAllNet() {
        if (groups.size() < 1) {
            initNetInfo();
        }
        return groups;
    }

    /**
     * 根据选中的序号来得到群的信息
     *
     * @param index
     * @return
     */
    public Cluster getNetByIndex(Integer index) {
        if (index < groups.size()) {
            selectIndex = index;
            return groups.get(index);
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

        if (c_id < clusterToGroup.size()) {
            Network net = clusterToGroup.get(c_id);
            return net.destroy(n_id);

        }

        return false;
    }


    public synchronized boolean moveNodeCluster() {
        int count = 0;
        for (int i = 0; i < pathData.size(); i++) {
            if (clusterPathIndex[i] < pathData.get(i).size()) {
                count++;
            }
        }
        if (count == 0) {

            return false;
        }
        for (int i = 0; i < groups.size(); i++) {
            if (clusterPathIndex[i] >= pathData.get(i).size()) {
                continue;
            }

            ArrayList<Network> networks = groups.get(i).getCluster();
            for (Network net : networks) {
                List<Node> nodeList = net.getNodeList();
                double[] location = pathData.get(i).get(clusterPathIndex[i]);
                double[] pre_location = pathData.get(i).get(clusterPathIndex[i] - 1);
                double lat, lon, hei;
                for (Node node : nodeList) {
                    if (node.getType() != -1) {
                        lon = node.getLongitude() + location[0] - pre_location[0] + (Math.random() - 0.5) * 0.002;
                        lat = node.getLatitude() + location[1] - pre_location[1] + (Math.random() - 0.5) * 0.002;
                        hei = node.getHeight() + location[2] - pre_location[2] + (Math.random() - 0.5) * 100;
                        node.setLatitude(lat);
                        node.setLongitude(lon);
                        node.setHeight(hei);
                        if (nodeList.get(net.getClusterId()).getType() == -1 || nodeList.get(net.getClusterId()).getEdges().size() < node.getEdges().size()) {
                            net.setClusterId(node.getId());
                        }
                        node.setType(i + 2);
                    }

                }
                net.getNodeList().get(net.getClusterId()).setType(0);
            }
            clusterPathIndex[i]++;
        }
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
        int len = groups.size();
        int[] flyTime = {95, 90, 200};
        int totalSeconds = 0;
        for (int i = 0; i < flyTime.length; i++) {
            totalSeconds = Math.max(totalSeconds, flyTime[i]);
        }
        int[] maxHeight = {30000, 300000, 10};
        double[][] start = new double[len][3];
        start[0] = new double[]{117.07, 31.75, 0};
        start[1] = new double[]{116.46, 28.97, 0};
        start[2] = new double[]{129.49, 29.60, 0};
        double[] end = {135.00, 22.60, 10};
        ArrayList<ArrayList<double[]>> data = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            ArrayList<double[]> c_i = new ArrayList<>(flyTime[i]);
            c_i.add(start[i]);
            double each_step_jing = (end[0] - start[i][0]) / flyTime[i];
            double each_step_wei = (end[1] - start[i][1]) / flyTime[i];
            int maxHeightTime = flyTime[i] / (i == 1 ? 2 : 10);
            int index;
            for (index = 1; index < maxHeightTime; index++) {
                double[] nextPosition = new double[3];
                double[] nowPosition = c_i.get(index - 1);
                nextPosition[0] = nowPosition[0] + each_step_jing;
                nextPosition[1] = nowPosition[1] + each_step_wei;
                nextPosition[2] = maxHeight[i] * getParabola(index, maxHeightTime);
                c_i.add(nextPosition);
            }
            for (; index <= flyTime[i] - maxHeightTime; index++) {
                double[] nextPosition = new double[3];
                double[] nowPosition = c_i.get(index - 1);
                nextPosition[0] = nowPosition[0] + each_step_jing;
                nextPosition[1] = nowPosition[1] + each_step_wei;
                nextPosition[2] = nowPosition[2];
                c_i.add(nextPosition);
            }
            for (; index <= flyTime[i]; index++) {
                double[] nextPosition = new double[3];
                double[] nowPosition = c_i.get(index - 1);
                nextPosition[0] = nowPosition[0] + each_step_jing;
                nextPosition[1] = nowPosition[1] + each_step_wei;
                double x = getParabola((flyTime[i] - index), maxHeightTime);
                nextPosition[2] = maxHeight[i] * x;
                c_i.add(nextPosition);
            }

            data.add(c_i);
        }
        this.pathData = data;
    }

    public List getPath(int c_i1, int n_i1, int c_i2, int n_i2) {
        countTransmit.get(0).increaseSend(c_i1, n_i1);
        int index = c_i1 == c_i2 ? 1 : 2;
        countTransmit.get(index).increaseSend(c_i1, n_i1);
        List<String> ans = new LinkedList<>();
        if (c_i1 >= allNets.size() || c_i2 >= allNets.size()) {
            return null;
        }
        Network net1 = allNets.get(c_i1);
        Network net2 = allNets.get(c_i2);
        if (n_i1 >= net1.getNodeList().size() || n_i2 >= net2.getNodeList().size()) {
            return null;
        }
        if (net1.getNodeList().get(n_i1).getType() == -1 || net2.getNodeList().get(n_i2).getType() == -1) {
            return null;
        }
        if (c_i1 == c_i2) {
            ArrayList<LinkedList<Integer>> paths = ShortPath.multiPathListBest(net1, n_i1, n_i2);
            LinkedList<Integer> path = paths.get(0);
            for (Integer p : path) {
                ans.add(c_i1 + "_" + p);
            }
            countTransmit.get(0).increaseRecv(c_i1, n_i1);
            countTransmit.get(index).increaseRecv(c_i1, n_i1);
            return ans;
        }

        int clusterId = net1.getClusterId();
        ArrayList<LinkedList<Integer>> paths;
        LinkedList<Integer> path;
        if (clusterId == n_i1) {
            ans.add(c_i1 + "_" + n_i1);
        } else {
            paths = ShortPath.multiPathListBest(net1, n_i1, clusterId);
            if (paths.size() == 0) return null;
            path = paths.get(0);
            for (Integer p : path) {
                ans.add(c_i1 + "_" + p);
            }
        }

        clusterId = net2.getClusterId();
        if (clusterId == n_i2) {
            ans.add(c_i2 + "_" + n_i2);
        } else {
            paths = ShortPath.multiPathListBest(net2, clusterId, n_i2);
            if (paths.size() == 0) return null;
            path = paths.get(0);
            for (Integer p : path) {
                ans.add(c_i2 + "_" + p);
            }
        }
        countTransmit.get(0).increaseRecv(c_i1, n_i1);
        countTransmit.get(index).increaseRecv(c_i1, n_i1);
        return ans;
    }

    public double getParabola(int index, int len) {
        int fenzi = index - len;
        int fenmu = len;
        double ans = 1 - fenzi * fenzi * 1.0 / (fenmu * fenmu);
        return ans;
    }

    public List<Network> getAllNetWork() {
        return allNets;
    }
}
