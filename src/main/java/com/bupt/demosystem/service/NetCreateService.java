package com.bupt.demosystem.service;

import com.bupt.demosystem.entity.Edge;
import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.entity.Node;
import com.bupt.demosystem.util.DRTD;
import com.bupt.demosystem.util.NetInfo;
import com.bupt.demosystem.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Banbridge on 2020/12/30.
 */
@Service
public class NetCreateService {


    private final int SIZE_OF_CLUSTER = 4;
    private final int MAX_COST = 60;
    private final int SIZE_OF_TYPE = 3;

    private final Logger logger = LoggerFactory.getLogger(NetCreateService.class);

    /**
     * 随机生成节点，利用DRTD算法生成拓扑网络
     *
     * */
    public Network getNetwork() {
        int n = 20;
        return getNetwork(n);
    }


    public Network getNetwork(int n) {
        logger.info(n + "生成拓扑图中，请稍后。。。。。");
        Random random = new Random();
        Network net = new Network();
        net.setId(-1);
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        HashMap<Integer, LinkedList<Integer>> cluster_map = new HashMap();
        int cap, type, clusterID;
        for (int i = 0; i < n; i++) {
            Node node = new Node();
            cap = 20 + random.nextInt(50);

            node.setX(5 + random.nextInt(90));
            node.setY(5 + random.nextInt(90));

            type = random.nextInt(SIZE_OF_TYPE);
            clusterID = getClusterArea(node.getX(), node.getY());
            if (i < 3) {
                type = 1;
            }
            if (!cluster_map.containsKey(clusterID)) {
                cluster_map.put(clusterID, new LinkedList<Integer>());
            }
            LinkedList<Integer> linklist_cluster = cluster_map.get(clusterID);
            node.setId(i);
            linklist_cluster.add(node.getId());
            cluster_map.put(clusterID, linklist_cluster);
            node.setCapacity(cap);
            node.setType(type);
            node.setCluster(clusterID);
            node.setIp("192.168.1." + i);

            nodes.add(node);
        }

        int[][] cost = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                cost[j][i] = cost[i][j] = i == j ? 0 :
                        (int) Math.sqrt(Math.pow(nodes.get(i).getX() - nodes.get(j).getX(), 2)
                                + Math.pow(nodes.get(i).getY() - nodes.get(j).getY(), 2));
            }

        }

        DRTD drtd = new DRTD();
        drtd.setCosts(cost);
        drtd.setMAX_COST(MAX_COST);
        drtd.setN(n);
        int[][] map = drtd.getMap();
        int edge_count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (map[i][j] == 1) {
                    edges.add(new Edge(edge_count++, i, j));
                }
            }
        }

        List<Double> invln = NetUtil.getInvulnerability(map);
        double netVal = 0;
        for (int i = 0; i < invln.size(); i++) {
            double nodeVal = invln.get(i);
            if (nodeVal != 0) {
                netVal += (nodeVal * Math.log(nodeVal));
            }
            Node node = nodes.get(i);
            node.setInvulnerability(nodeVal);
            nodes.set(i, node);
        }
        net.setNetValue(-netVal);
        net.setNodeList(nodes);
        net.setEdgeList(edges);
        logger.info("生成成功，规模为:" + n + " " + net.getNetValue());
        return net;
    }

    private int getClusterArea(int x, int y) {
        if (x < 50) {
            if (y < 50) {
                return 0;
            }
            return 2;
        }
        if (x >= 50) {
            if (y < 50) {
                return 1;
            }
            return 3;
        }

        return 0;
    }

}
