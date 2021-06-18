package com.bupt.demosystem.aodv.message;

import java.net.InetSocketAddress;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author banbridge
 * @Classname RoutingNeighbor
 * @Date 2021/6/11 15:43
 */
public class RoutingNeighbor implements Runnable {

    /**
     * 节点的id
     */
    private int delay;

    private HashMap<InetSocketAddress, Neighbor> neighbors;

    private ScheduledThreadPoolExecutor exec;


    public RoutingNeighbor(int delay, ScheduledThreadPoolExecutor exec) {
        this.delay = delay;
        this.exec = exec;
        neighbors = new HashMap<>();
    }

    /**
     * Return expire time for neighbor node with address addr, if exists, else return 0.
     * \param addr the IP address of the neighbor node
     * \returns the expire time for the neighbor node
     */
    public LocalTime getExpireTime(InetSocketAddress addr) {
        purge();
        Neighbor neighbor = neighbors.get(addr);
        return neighbor == null ? LocalTime.MIN : neighbor.getExpireTime();
    }

    /**
     * Check that node with address addr is neighbor
     * \param addr the IP address to check
     * \returns true if the node with IP address is a neighbor
     */
    public boolean isNeighbor(InetSocketAddress addr) {
        purge();
        return neighbors.containsKey(addr);
    }

    /**
     * Update expire time for entry with address addr, if it exists, else add new entry
     * \param addr the IP address to check
     * \param expire the expire time for the address
     */
    public void update(InetSocketAddress addr, LocalTime expire) {
        Neighbor neighbor = neighbors.get(addr);
        if (expire.isBefore(neighbor.getExpireTime())) {
            expire = neighbor.getExpireTime();
        }
        neighbor.setExpireTime(expire);
        purge();
    }

    /**
     * Remove all expired entries
     */
    void purge() {
        if (neighbors.isEmpty()) {
            return;
        }
        neighbors.entrySet().removeIf((entry) -> {
            return entry.getValue().getExpireTime().isBefore(LocalTime.now());
        });
    }

    /**
     * Schedule m_ntimer.
     */

    public void scheduleTimer() {
        exec.schedule(this, 1, TimeUnit.SECONDS);
    }

    /**
     * Remove all entries
     */

    public void clear() {
        neighbors.clear();
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        purge();
    }
}


class Neighbor {

    /**
     * 邻居的节点id
     */
    private int neighborId;

    /**
     * 邻居节点的通信地址
     */
    private InetSocketAddress neighborAddress;

    /**
     * 过期时间
     */
    private LocalTime expireTime;

    /**
     * 是否关闭
     */
    private boolean closed;

    public Neighbor(int neighborId, InetSocketAddress neighborAddress, LocalTime expireTime) {
        this.neighborId = neighborId;
        this.neighborAddress = neighborAddress;
        this.expireTime = expireTime;
        this.closed = false;
    }

    public int getNeighborId() {
        return neighborId;
    }

    public void setNeighborId(int neighborId) {
        this.neighborId = neighborId;
    }

    public InetSocketAddress getNeighborAddress() {
        return neighborAddress;
    }

    public void setNeighborAddress(InetSocketAddress neighborAddress) {
        this.neighborAddress = neighborAddress;
    }

    public LocalTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalTime expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
