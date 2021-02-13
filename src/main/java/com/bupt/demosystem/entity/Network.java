package com.bupt.demosystem.entity;


import java.util.List;

/**
 * Created by Banbridge on 2020/12/30.
 */
public class Network {
    private int id;
    private List<Node> nodeList;
    private List<Edge> edgeList;
    private int cost[][];

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }

    public List<Edge> getEdgeList() {
        return edgeList;
    }

    public void setEdgeList(List<Edge> edgeList) {
        this.edgeList = edgeList;
    }

    public int[][] getCost() {
        return cost;
    }

    public void setCost(int[][] cost) {
        this.cost = cost;
    }
}
