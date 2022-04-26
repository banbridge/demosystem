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


    private final int MAX_COST = 50;
    private static int id = 0;
    private final int MAXNODECOUNT = 22;
    private final Logger logger = LoggerFactory.getLogger(NetCreateService.class);

    /**
     * 随机生成节点，利用DRTD算法生成拓扑网络
     */
    public Network getNetwork() {
        int n = 20;
        return getNetwork(n);
    }

    public ArrayList<Network> getNetworkList(int n) {
        return getNetworkList(n, "192.168.0", 1);
    }

    public ArrayList<Network> getNetworkList(int n, String ipBase, int type) {
        id = 0;
        int parts = n / MAXNODECOUNT;
        int rest = n % parts;
        if (parts == 0 || rest > MAXNODECOUNT / 2) {
            parts++;
        }
        int[] perNodes = new int[parts];
        int sum = 0;
        int perCluster = n / parts;
        ArrayList<Network> clusters = new ArrayList<>(parts);
        for (int i = 0; i < parts; i++) {
            if (i == parts - 1) {
                perNodes[i] = n - sum;
            } else {
                perNodes[i] = perCluster;
                sum += perCluster;
            }
            Network network = new Network();
            network.setId(-1);
            List<Node> nodes = new ArrayList<>();
            network.setNodeList(nodes);
            clusters.add(network);
        }

        int height = parts > 3 ? 2 : 1;
        int width = parts / height;
        logger.info(n + "生成拓扑图中，请稍后。。。。。");
        Random random = new Random();

        List<Node> nodes = new ArrayList<>(n + 10);
        int cap;
        for (int i = 0; i < n; i++) {
            Node node = new Node();
            cap = 20 + random.nextInt(50);
            node.setX(5 + random.nextInt(90));
            node.setY(5 + random.nextInt(90));
            node.setId(id++);
            node.setCapacity(cap);
            node.setType(i > 0 ? type : 0);
            node.setIp(ipBase + i);
            nodes.add(node);
        }

        int cellWidth = 100 / width;
        int cellHeight = 100 / height;
        for (Node node : nodes) {
            int c = node.getX() / cellWidth;
            int r = node.getY() / cellHeight;
            clusters.get(c * width + r).getNodeList().add(node);
        }

        System.out.println("-------------------------");
        System.out.println(clusters.size());
        for (Network cluster : clusters) {
            System.out.println(cluster.getNodeList().size());
            Network clusetrNetwork = getNetwork(cluster);
            clusters.add(clusetrNetwork);
            System.out.println(clusetrNetwork == null);
        }
        return clusters;
    }

    public Network getNetwork(Network net) {

        List<Node> nodes = net.getNodeList();
        int n = nodes.size();
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


        double[] invln = NetUtil.getInvulnerability(map);
        double netVal = 0;
        double nodeVal = 0.0;

        for (int i = 0; i < n; i++) {
            nodeVal = invln[i];
            if (nodeVal != 0) {
                netVal += (nodeVal * Math.log(nodeVal));
            }
            Node node = nodes.get(i);
            node.setInvulnerability(nodeVal);
            for (int j = i + 1; j < n; j++) {
                if (map[i][j] == 1) {
                    node.addEdge(j);
                    nodes.get(j).addEdge(i);
                }
            }
        }
        net.setNetValue(-netVal);
        net.setNodeList(nodes);
        logger.info("生成成功，规模为:" + n + " " + net.getNetValue());
        return net;
    }

    public Network getNetwork(int n) {
        return getNetwork(n, "192.168.0.");
    }

    public Network getNetwork(int n, String ipBase) {
        return getNetwork(n, ipBase, 1);
    }

    public Network getNetwork(int n, String ipBase, int type) {
        return getNetwork(n, ipBase, type, 1);
    }

    public Network getNetwork(int n, String ipBase, int type, int part) {
        logger.info(n + " getNetwork 生成拓扑图中，请稍后。。。。。");
        Random random = new Random();
        Network net = new Network();
        net.setId(-1);
        List<Node> nodes = new ArrayList<>(n + 10);
        int cap;
        for (int i = 0; i < n; i++) {
            Node node = new Node();
            cap = 20 + random.nextInt(50);

            node.setX(5 + random.nextInt(90));
            node.setY(5 + random.nextInt(90));
            node.setId(id++);
            node.setCapacity(cap);
            node.setType(i > 0 ? type : 0);
            node.setIp(ipBase + i);
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


        double[] invln = NetUtil.getInvulnerability(map);
        double netVal = 0;
        double nodeVal = 0.0;

        for (int i = 0; i < n; i++) {
            nodeVal = invln[i];
            if (nodeVal != 0) {
                netVal += (nodeVal * Math.log(nodeVal));
            }
            Node node = nodes.get(i);
            node.setInvulnerability(nodeVal);
            for (int j = i + 1; j < n; j++) {
                if (map[i][j] == 1) {
                    node.addEdge(j);
                    nodes.get(j).addEdge(i);
                }
            }
        }
        net.setNetValue(-netVal);
        net.setNodeList(nodes);
        logger.info("生成成功，规模为:" + n + " " + net.getNetValue());
        return net;
    }


}
