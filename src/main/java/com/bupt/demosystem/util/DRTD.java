package com.bupt.demosystem.util;

/**
 * Created by Banbridge on 2021/1/8.
 */

public class DRTD {

    private int MAX_COST;
    private int[][] costs;
    private int[] nodes;
    private int n;
    private int[][] map;

    public int[][] getMap() {
        map = new int[n][n];
        boolean[][] whether_con = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                whether_con[i][j] = whether_con[j][i] = costs[i][j] < MAX_COST ? true : false;
            }
        }


        return map;
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

    public int[] getNodes() {
        return nodes;
    }

    public void setNodes(int[] nodes) {
        this.nodes = nodes;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }
}
