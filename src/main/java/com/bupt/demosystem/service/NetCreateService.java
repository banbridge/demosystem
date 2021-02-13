package com.bupt.demosystem.service;

import com.bupt.demosystem.entity.Edge;
import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.entity.Node;
import com.bupt.demosystem.util.DRTD;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Banbridge on 2020/12/30.
 */
@Service
public class NetCreateService {

    /*
     * 随机生成节点，利用DRTD算法生成拓扑网络
     * */
    public Network getNetwork() {
        int n = 20, e = n * 2;
        return getNetwork(n, e);
    }

    public Network getNetwork(int n, int e) {
        Random random = new Random();
        Network net = new Network();
        net.setId(0);
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        int from, to;

        for (int i = 0; i < n; i++) {
            Node node = new Node();
            int cap = 20 + random.nextInt(50);
            int type = random.nextInt(3);
            node.setId(i);
            node.setCapacity(cap);
            node.setType(type);
            node.setIp("192.168.1." + i);
            node.setX(5 + random.nextInt(90));
            node.setY(5 + random.nextInt(90));
            nodes.add(node);
        }


        for (int i = 0; i < e; i++) {
            from = random.nextInt(n);
            to = random.nextInt(n);
            if (from == to) {
                to = (from + 1) % n;
            }
            edges.add(new Edge(i, from, to));
        }

        net.setNodeList(nodes);
        net.setEdgeList(edges);
        return net;
    }

    public Network getNetwork(int n) {
        Random random = new Random();
        Network net = new Network();
        net.setId(1);

        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Node node = new Node();
            int cap = 20 + random.nextInt(50);
            int type = random.nextInt(3);
            if (i < 3) {
                type = 1;
            }
            node.setId(i);
            node.setCapacity(cap);
            node.setType(type);
            node.setIp("192.168.1." + i);
            node.setX(5 + random.nextInt(90));
            node.setY(5 + random.nextInt(90));
            nodes.add(node);
        }

        int cost[][] = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                cost[i][j] = (i == j ? 0 : random.nextInt(100));
            }

        }

        net.setCost(cost);

        int MAX_COST = 50;
        DRTD drtd = new DRTD();
        drtd.setCosts(net.getCost());
        drtd.setMAX_COST(MAX_COST);
        drtd.setN(n);
        int[][] map = drtd.getMap();
        int edge_count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (map[i][j] == 1) {
                    edges.add(new Edge(edge_count, i, j));
                }
            }
        }

        net.setNodeList(nodes);
        net.setEdgeList(edges);
        return net;
    }

}
