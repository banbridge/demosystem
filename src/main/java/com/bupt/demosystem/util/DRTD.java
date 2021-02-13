package com.bupt.demosystem.util;

import java.util.*;

/**
 * Created by Banbridge on 2021/1/8.
 */

public class DRTD {

    private int MAX_COST;
    private int[][] costs;
    private int n;
    private int[][] map;
    private ArrayList<Integer> node_scan;
    private LinkedList<Integer> node_unscan;
    private int[] degree;
    boolean[][] whetherNeighbor;
    int M0_degree;

    public int[][] getMap() {
        map = new int[n][n];
        degree = new int[n];
        //已经扫描的节点
        node_scan = new ArrayList<>();
        //剩余未扫描的节点
        node_unscan = new LinkedList<Integer>();
        for (int i = 0; i < n; i++) {
            node_unscan.addLast(i);
            for (int j = 0; j < n; j++) {
                map[i][j] = (i == j ? 0 : ShortPath.MAX);
            }
        }
        whetherNeighbor = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                whetherNeighbor[i][j] = whetherNeighbor[j][i] = (costs[i][j] <= MAX_COST);
            }
        }

        //假设初始两个节点连接
        int first = node_unscan.removeFirst();
        node_scan.add(first);
        int two = node_unscan.removeFirst();
        node_scan.add(two);
        connectNodes(first, two);
        while (!node_unscan.isEmpty()) {
            int now = node_unscan.remove();
            node_scan.add(now);
            solveNode(now);
        }
        return map;
    }


    private void solveNode(int now) {
        ArrayList<Integer> nodeNeighbor = new ArrayList<>();
        for (int node : node_scan) {
            if (whetherNeighbor[node][now]) {
                nodeNeighbor.add(node);
            }
        }
        M0_degree = 0;
        for (int node : nodeNeighbor) {
            M0_degree += degree[node];
        }

        for (int node : nodeNeighbor) {
            solveNeigbor(nodeNeighbor.size(), node, now);
        }

    }

    private void solveNeigbor(int M0, int node, int now) {
        int sizeOfMap = node_scan.size();
        double K_i = (M0 * 1.0 / sizeOfMap) * (degree[node] * 1.0 / M0_degree);

        double BC_i = 0;
        connectNodes(node, now);
        for (int node1 : node_scan) {
            for (int node2 : node_scan) {
                if (node1 != node2 && node1 != node) {
                    ArrayList<LinkedList<Integer>> path_sigma = ShortPath.multiPath(this.map, node1, node2);
                    if (path_sigma.size() != 0) {
                        int sigma_fenmu = path_sigma.size();
                        int sigma_fenzi = 0;
                        for (int i = 0; i < sigma_fenmu; i++) {
                            if (path_sigma.get(i).contains(node)) {
                                sigma_fenzi++;
                            }
                        }
                        BC_i += (1.0 * sigma_fenzi / sigma_fenmu);
                    }
                }
            }
        }
        BC_i = BC_i * (1.0 / (sizeOfMap - 1) * (sizeOfMap - 2));

        int ans = (int) (BC_i * K_i * 100);

        int rate_three = getRandomTimes(3);
        if (rate_three >= ans) {
            unconnectNodes(node, now);
        }
    }

    private int getRandomTimes(int times) {
        Random random = new Random();
        int sum = 0;
        for (int i = 0; i < times; i++) {
            sum += random.nextInt(100);
        }
        sum /= times;
        return sum;
    }

    private void connectNodes(int from, int to) {
        this.map[from][to] = this.map[to][from] = 1;
        degree[from]++;
        degree[to]++;
    }

    private void unconnectNodes(int from, int to) {
        this.map[from][to] = this.map[to][from] = ShortPath.MAX;
        degree[from]--;
        degree[to]--;
    }

    public int getMAX_COST() {
        return MAX_COST;
    }

    public void setMAX_COST(int MAX_COST) {
        this.MAX_COST = MAX_COST;
    }

    public int[][] getCosts() {
        return costs;
    }

    public void setCosts(int[][] costs) {
        this.costs = costs;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }
}
