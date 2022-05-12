package com.bupt.demosystem.util;

import com.bupt.demosystem.config.SettingConfig;
import com.bupt.demosystem.entity.Cluster;
import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.entity.Node;
import com.bupt.demosystem.service.NetCreateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author banbridge
 * @Classname Group
 * @Date 2022/4/26 10:09
 */

@Service
public class Group {


    private final SettingConfig settingConfig;

    private final int SIZEOFNET = 3;

    private int selectIndex;
    public Logger logger = LoggerFactory.getLogger(Group.class);

    //三个群的路径索引
    private int[] clusterPathIndex;
    String ipBase = "192.168.";


    private ArrayList<ArrayList<double[]>> pathData;

    private CountTransmit countTransmit;
    private Map<Integer, Network> clusterToGroup;


    //簇的存储
    private ArrayList<Cluster> groups;


    public Group(SettingConfig settingConfig) {
        this.settingConfig = settingConfig;
        groups = new ArrayList<>(5);
        clusterToGroup = new HashMap<>();
        selectIndex = 0;
        initNetInfo();
    }

    /**
     * 进行一些初始化操作，或者重新请求仿真时候
     */
    public void initNetInfo() {
        groups.clear();


        NetCreateService netCreateService = new NetCreateService();
        int[] nodeSize = settingConfig.getNodeSize();

        for (int i = 1; i <= settingConfig.getFlyTime().length; i++) {
            ArrayList<Network> nets = netCreateService.getNetworkList(nodeSize[i - 1], ipBase + i + ".", i);
            Cluster cls = new Cluster();
            cls.setCluster(nets);
            //logger.info("------"+cls.getCluster().size());
            groups.add(cls);
        }
        updateClusterToGroup();

        clusterPathIndex = new int[groups.size()];
        getPathData();
        for (int i = 0; i < groups.size(); i++) {
            int cnt = 1;
            for (Network network : groups.get(i).getCluster()) {
                List<Node> nodes = network.getNodeList();
                double[] poi = pathData.get(i).get(clusterPathIndex[i]);
                for (Node node : nodes) {
                    node.setLatitude(poi[1] + (node.getX() - 50) * 1.0 / 100 * 0.09);
                    node.setLongitude(poi[0] + (node.getY() - 50) * 1.0 / 100 * 0.09);
                    node.setHeight(poi[2]);
                    node.setType(cnt);
                }
                nodes.get(0).setType(0);
                network.setId(cnt++);
            }

            clusterPathIndex[i]++;
        }
        countTransmit = new CountTransmit(3);
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

        for (int i = 0; i < groups.size(); i++) {
            double val = 0;
            int size = 0;
            Cluster nets = groups.get(i);
            int cnt = 1;
            for (Network net : nets.getCluster()) {
                val += net.getNetValue();
                size += net.getNodeList().size();
                for (Node node : net.getNodeList()) {
                    clusterToGroup.put(node.getId(), net);
                }
                net.setId(cnt++);
            }
            nets.setNetValue(val / nets.getCluster().size());
            nets.setSize(size);
        }
    }


    public CountTransmit getCountTransmit() {
        return countTransmit;
    }

    public void setCountTransmit(CountTransmit countTransmit) {
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
     * @param n_id
     */
    public boolean destroyNode(int n_id) {


        Network net = clusterToGroup.get(n_id);

        if (net == null) {
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
                Node ch = null;
                for (Node node : nodeList) {
                    if (node.getType() != -1) {
                        lon = node.getLongitude() + location[0] - pre_location[0] + (Math.random() - 0.5) * 0.002;
                        lat = node.getLatitude() + location[1] - pre_location[1] + (Math.random() - 0.5) * 0.002;
                        hei = node.getHeight() + location[2] - pre_location[2] + (Math.random() - 0.5) * 100;
                        node.setLatitude(lat);
                        node.setLongitude(lon);
                        node.setHeight(hei);
                        if (ch == null || ch.getEdges().size() < node.getEdges().size()) {
                            ch = node;
                        }
                        node.setType(i + 2);
                    }

                }
                assert ch != null;
                ch.setType(0);
            }
            clusterPathIndex[i]++;
        }
        return true;
    }


    public ArrayList<ArrayList<double[]>> getPathData() {
        setPathData(null);
        return pathData;
    }


    /**
     * 计算输入最大高度，三个群的起始位置，三个群的终点位置，计算预估路径
     *
     * @param pathData
     */
    public void setPathData(ArrayList<ArrayList<double[]>> pathData) {
        int len = groups.size();
        int[] flyTime = settingConfig.getFlyTime();
        int totalSeconds = 0;
        for (int i = 0; i < flyTime.length; i++) {
            totalSeconds = Math.max(totalSeconds, flyTime[i]);
        }
        int[] maxHeight = settingConfig.getMaxHeight();
        double[][] start = new double[len][3];
        double[] end = settingConfig.getEndPos();
        double[] startPP = settingConfig.getStartPos();
        for (int i = 0; i < len * 3; i++) {
            start[i / 3][i % 3] = startPP[i];
        }

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

    public ArrayList<LinkedList<Integer>> getPath(int start, int end) {
        Network networkStart = clusterToGroup.getOrDefault(start, null);
        Network networkEnd = clusterToGroup.getOrDefault(end, null);
        if (networkEnd == null || networkStart == null) {
            return null;
        }
        if (networkEnd == networkStart) {
            return ShortPath.multiPathList(networkStart, start, end);
        }
        int start_c = networkStart.getClusterId();
        int end_c = networkEnd.getClusterId();
        ArrayList<LinkedList<Integer>> path1 = ShortPath.multiPathList(networkStart, start, start_c);
        ArrayList<LinkedList<Integer>> path2 = ShortPath.multiPathList(networkEnd, end_c, end);
        if (path1.size() < 1 || path2.size() < 1) {
            return null;
        }
        ArrayList<LinkedList<Integer>> ans = new ArrayList<>();
        LinkedList<Integer> best = new LinkedList<>();
        best.addAll(path1.get(0));
        best.addAll(path2.get(0));
        ans.add(best);
        return ans;
    }

    public List getPathCount(int start, int end) {

        ArrayList<LinkedList<Integer>> ans = getPath(start, end);
        int cid1 = clusterToGroup.get(start).getId() - 1;
        int cid2 = clusterToGroup.get(end).getId() - 1;
        int index = cid1 == cid2 ? 1 : 2;
        countTransmit.increaseSend(start, index);
        if (ans.size() < 1 || ans.get(0).size() < 2) {
            return null;
        }
        countTransmit.increaseRecv(start, index);
        return ans.get(0);
    }

    public double getParabola(int index, int len) {
        int fenzi = index - len;
        int fenmu = len;
        double ans = 1 - fenzi * fenzi * 1.0 / (fenmu * fenmu);
        return ans;
    }

    public List<Network> getAllNetWork() {
        List<Network> allNets = new ArrayList<>();
        for (Cluster c : groups) {
            allNets.addAll(c.getCluster());
        }
        return allNets;
    }
}
