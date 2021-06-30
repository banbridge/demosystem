package com.bupt.demosystem.aodv.message;

import org.apache.tomcat.jni.Local;

import java.net.InetSocketAddress;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.LinkedList;

/**
 * @Author banbridge
 * @Classname IdCache
 * @Date 2021/6/10 17:06
 */
public class IdCache {

    private final LinkedList<UniqueId> idCache;
    private int lifeTime;

    public IdCache() {
        idCache = new LinkedList<>();
    }

    public IdCache(int lifeTime) {
        this.lifeTime = lifeTime;
        idCache = new LinkedList<>();
    }

    private void purge() {
        idCache.removeIf(id -> id.expire.isBefore(LocalTime.now()));
    }

    public boolean isDuplicate(InetSocketAddress addr, int id) {
        purge();
        for (UniqueId uniqueId : idCache) {
            if (uniqueId.address.equals(addr) && uniqueId.id == id) {
                return true;
            }
        }
        idCache.push(new UniqueId(addr, id, LocalTime.now().plus(lifeTime, ChronoUnit.MILLIS)));
        return false;
    }

    public int getSize() {
        purge();
        return idCache.size();
    }


    public int getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    private class UniqueId {
        InetSocketAddress address;
        int id;
        LocalTime expire;

        public UniqueId(InetSocketAddress address, int id, LocalTime expire) {
            this.address = address;
            this.id = id;
            this.expire = expire;
        }
    }

}
