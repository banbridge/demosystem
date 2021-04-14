package com.bupt.demosystem.entity;


/**
 * Created by Banbridge on 2020/12/30.
 */

public class Node {
    private int id;
    private String ip;
    private int capacity;
    private int type;
    private int x;
    private int y;
    private int cluster;
    private double invulnerability;

    public double getInvulnerability() {
        return invulnerability;
    }

    public void setInvulnerability(double invulnerability) {
        this.invulnerability = invulnerability;
    }

    public Node(int id, String ip) {
        this.id = id;
        this.ip = ip;
    }

    public Node(int id, String ip, int capacity, int type) {
        this.id = id;
        this.ip = ip;
        this.capacity = capacity;
        this.type = type;
    }

    public Node() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }


    public int getCluster() {
        return cluster;
    }

    public void setCluster(int cluster) {
        this.cluster = cluster;
    }
}
