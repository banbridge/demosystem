package com.bupt.demosystem.entity;


import java.util.*;

/**
 * Created by Banbridge on 2020/12/30.
 */
public class Network {

    private Integer id;
    //节点列表
    private List<Node> nodeList;
    private List<Edge> edgeList;
    private String modifiedTime;
    //抗毁度值 
    private double netValue;

    public int getId() {
        return id;
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

    public void setId(Integer id) {
        this.id = id;
    }


    public String getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public double getNetValue() {
        return netValue;
    }

    public void setNetValue(double netValue) {
        this.netValue = netValue;
    }
}
