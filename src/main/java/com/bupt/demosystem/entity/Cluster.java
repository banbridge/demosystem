package com.bupt.demosystem.entity;

import com.bupt.demosystem.entity.Network;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author banbridge
 * @Classname Cluster
 * @Date 2022/4/26 10:14
 */
public class Cluster {

    private ArrayList<Network> cluster;
    private static int ID = 0;
    private int id;
    private String modifiedTime;
    private double netValue;

    public Cluster() {
        id = ID++;
    }

    public ArrayList<Network> getCluster() {
        return cluster;
    }

    public void setCluster(ArrayList<Network> cluster) {
        this.cluster = cluster;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
