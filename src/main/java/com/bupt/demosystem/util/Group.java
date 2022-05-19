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
    private double[] cellWidth = {0.09, 0.09, 0.13};
    String ipBase = "192.168.";


    private ArrayList<ArrayList<double[]>> pathData;

    private CountTransmit countTransmit;
    private ArrayList<CountTransmit> allCountTransmit;
    private Map<Integer, Network> clusterToGroup;
    private Map<Integer, Node> idToNode;


    //簇的存储
    private ArrayList<Cluster> groups;

    // 记录破坏的节点数量，有个递增的过程
    private int sizeOfDestroyedNode;


    public Group(SettingConfig settingConfig) {
        this.settingConfig = settingConfig;
        groups = new ArrayList<>(5);
        clusterToGroup = new HashMap<>();
        idToNode = new HashMap<>();
        selectIndex = 0;
        initNetInfo();
    }

    /**
     * 进行一些初始化操作，或者重新请求仿真时候
     */
    public void initNetInfo() {
        allCountTransmit = new ArrayList<>();
        if (groups.size() < 1) {
            int[] nodeSize = settingConfig.getNodeSize();
            NetCreateService netCreateService = new NetCreateService();
            for (int i = 1; i <= nodeSize.length; i++) {
                ArrayList<Network> nets = netCreateService.getNetworkList(nodeSize[i - 1], ipBase + i + ".", i);
                Cluster cls = new Cluster();
                cls.setCluster(nets);
                //logger.info("------"+cls.getCluster().size());
                groups.add(cls);
            }
            updateClusterToGroup();
        }
        getPathData();
        initSimulatorStatus();
        sizeOfDestroyedNode = 0;
    }

    public void initSimulatorStatus() {
        //logger.info("初始化为第"+getSizeOfDestroyedNode()+"次仿真");
        clusterPathIndex = new int[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            int cnt = 1;
            for (Network network : groups.get(i).getCluster()) {
                List<Node> nodes = network.getNodeList();
                double[] poi = pathData.get(i).get(clusterPathIndex[i]);
                for (Node node : nodes) {
                    node.setLatitude(poi[1] + (node.getX() - 50) * 1.0 / 100 * cellWidth[i]);
                    node.setLongitude(poi[0] + (node.getY() - 50) * 1.0 / 100 * cellWidth[i]);
                    node.setHeight(poi[2]);
                    node.setType(cnt);
                }
                nodes.get(0).setType(0);
                network.setId(cnt++);
            }
        }
        countTransmit = new CountTransmit(3);
        countTransmit.setTotalNode(settingConfig.getSumNode());
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
        int sum = 0;
        for (Network network : cls.getCluster()) {
            sum += network.getNodeList().size();
            int cnt = 0;
            for (Node node : network.getNodeList()) {
                node.setIp(String.format("%s%d.%d", ipBase, selectIndex + 1, cnt++));
            }
        }
        groups.set(selectIndex, cls);
        updateClusterToGroup();
        initSimulatorStatus();
        int[] nodeSize = settingConfig.getNodeSize();
        nodeSize[selectIndex] = sum;
        settingConfig.setNodeSize(nodeSize);
    }

    private void updateClusterToGroup() {
        clusterToGroup.clear();
        idToNode.clear();
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
                    idToNode.put(node.getId(), node);
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
     * 破坏n个节点
     */
    public Integer[] destroyNNodes(int n) {
        Integer[] ids = clusterToGroup.keySet().toArray(new Integer[0]);
        n = Math.min(n, ids.length);
        Random rand = new Random();
        for (int i = 1; i <= n; i++) {
            int id_index = rand.nextInt(ids.length - i);
            //System.out.println(id_index);
            destroyNode(ids[id_index]);
            swap(ids, ids.length - i, id_index);
        }
        Integer[] ans = Arrays.copyOfRange(ids, ids.length - n, ids.length);
        logger.info(String.format("故障%d个节点，故障节点id为%s\n", n, Arrays.toString(ans)));
        return ids;
    }

    private <T> void swap(T[] ids, int i, int j) {
        T tem = ids[i];
        ids[i] = ids[j];
        ids[j] = tem;
    }

    /**
     * 根据簇的id和节点id毁坏节点
     *
     * @param n_id
     */
    public boolean destroyNode(int n_id) {
        Node node = idToNode.get(n_id);
        //logger.info(String.format("故障节点id为%d, 地址为:%s\n", n_id, node));
        if (node != null) {
            node.setType(-1);
            return true;
        }
        return false;
    }


    /**
     * 返回-1表示此轮仿真结束，进行初始化操作，
     * 返回-2表示所有的仿真结束
     * 返回大于等于0的时候表示此轮破坏多少个节点
     *
     * @return
     */
    public synchronized int moveNodeCluster() {
        int count = 0;
        for (int i = 0; i < pathData.size(); i++) {
            if (clusterPathIndex[i] < pathData.get(i).size() - 1) {
                count++;
            }
        }
        if (count == 0) {
            allCountTransmit.add(countTransmit);
            if (sizeOfDestroyedNode == settingConfig.getNumOfBadNode()) {
                return -2;
            } else {
                sizeOfDestroyedNode++;
                initSimulatorStatus();
                destroyNNodes(sizeOfDestroyedNode);
                return -1;
            }

        }
        logger.info("pathIndex: " + Arrays.toString(clusterPathIndex) + sizeOfDestroyedNode + "," + settingConfig.getNumOfBadNode());
        for (int i = 0; i < groups.size(); i++) {
            clusterPathIndex[i] = Math.min(clusterPathIndex[i] + settingConfig.getSpeed(), pathData.get(i).size()-1);
            ArrayList<Network> networks = groups.get(i).getCluster();
            for (Network net : networks) {
                List<Node> nodeList = net.getNodeList();
                double[] poi = pathData.get(i).get(clusterPathIndex[i]);
                Node ch = null;
                for (Node node : nodeList) {
                    if (node.getType() != -1) {
                        node.setLatitude(poi[1] + (node.getX() - 50) * 1.0 / 100 * cellWidth[i]);
                        node.setLongitude(poi[0] + (node.getY() - 50) * 1.0 / 100 * cellWidth[i]);
                        node.setHeight(poi[2]);
                        if (ch == null || ch.getEdges().size() < node.getEdges().size()) {
                            ch = node;
                        }
                        node.setType(i + 2);
                    }

                }
                assert ch != null;
                ch.setType(0);
                net.setClusterId(ch.getId());
            }

        }
        return sizeOfDestroyedNode;
    }


    /**
     * 重复仿真多少次
     *
     * @param n
     * @return
     */
    public ArrayList<CountTransmit> simulatorTimes(int n) {
        ArrayList<CountTransmit> countTransmits = new ArrayList<>();

        int dataNumber = 170;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= settingConfig.getNumOfBadNode(); j++) {
                //初始化节点信息
                initSimulatorStatus();
                //移动一次 更新簇首信息
                moveNodeCluster();
                //破坏J个节点
                Integer[] ids = destroyNNodes(j);
                Random ran = new Random();
                for (int k = 0; k < dataNumber; k++) {
                    int len = ids.length - 1;
                    int cccc = ran.nextInt(100);
                    if (cccc < 90) {
                        len = len - j;
                    }
                    int index = ran.nextInt(len);
                    int start = ids[index];
                    index = ran.nextInt(len);
                    int end = ids[index];
                    getPathCount(start, end);
                }
                this.countTransmit.setDestroyNode(j);
                if (i == 0) {
                    countTransmits.add(getCountTransmit());
                } else {
                    countTransmits.get(j).addCountTransmit(getCountTransmit());

                }
            }
        }
        for (int j = 0; j <= settingConfig.getNumOfBadNode(); j++) {
            countTransmits.get(j).divideCountTransmit(n);
        }
        logger.info(String.format("仿真%d次，仿真参数为：%s \n", n, settingConfig.toString()));
        return countTransmits;
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
        if (start == end) {
            ArrayList<LinkedList<Integer>> ans = new ArrayList<>();
            LinkedList<Integer> list = new LinkedList<>();
            list.add(start);
            list.add(end);
            ans.add(list);
            return ans;
        }
        if (networkEnd == networkStart) {
            return ShortPath.multiPathList(networkStart, start, end);
        }
        int start_c = networkStart.getClusterId();
        int end_c = networkEnd.getClusterId();
        ArrayList<LinkedList<Integer>> path1 = ShortPath.multiPathList(networkStart, start, start_c);
        ArrayList<LinkedList<Integer>> path2 = ShortPath.multiPathList(networkEnd, end_c, end);
        if (path1 == null || path2 == null || path1.size() < 1 || path2.size() < 1) {
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
        if (ans == null || ans.size() < 1 || ans.get(0).size() < 2) {
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

    public ArrayList<CountTransmit> getAllCountTransmit() {
        return allCountTransmit;
    }

    public void setAllCountTransmit(ArrayList<CountTransmit> allCountTransmit) {
        this.allCountTransmit = allCountTransmit;
    }

    public int getSizeOfDestroyedNode() {
        return sizeOfDestroyedNode;
    }


}


enum Status {
    init, stop, run
}