package com.bupt.demosystem.entity;


import org.springframework.data.relational.core.sql.In;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Banbridge on 2020/12/30.
 */

public class Node {
    private int id;
    private String ip;
    private int capacity;
    // -1损坏，0是簇首，其他是普通结点
    private int type;
    private int x, y;
    private double latitude, longitude, height;
    private double invulnerability;
    private LinkedList<Integer> edges = new LinkedList<>();

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
        this.edges = new LinkedList<>();
    }

    /**
     * 节点新增一个连接节点
     *
     * @param index
     */
    public void addEdge(Integer index) {
        edges.add(index);
    }

    /**
     * 移除一个连接节点
     *
     * @param index
     */
    public boolean removeEdge(Integer index) {
        return edges.remove(index);
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

    public LinkedList<Integer> getEdges() {
        return edges;
    }

    public void setEdges(LinkedList<Integer> edges) {
        this.edges = edges;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
