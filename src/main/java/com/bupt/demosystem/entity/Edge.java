package com.bupt.demosystem.entity;


/**
 * Created by Banbridge on 2020/12/30.
 */
public class Edge {
    private int id;
    private int from;
    private int to;

    public Edge() {
    }

    public Edge(int id, int from, int to) {
        this.id = id;
        this.from = from;
        this.to = to;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }
}
