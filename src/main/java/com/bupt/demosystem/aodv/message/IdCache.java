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

    private LinkedList<UniqueId> m_idCache;
    private int lifeTime;

    public IdCache() {
        m_idCache = new LinkedList<>();
    }

    public IdCache(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    private void purge() {
        m_idCache.removeIf(id -> id.m_expire.isBefore(LocalTime.now()));
    }

    public boolean isDuplicate(InetSocketAddress addr, int id) {
        purge();
        for (UniqueId uniqueId : m_idCache) {
            if (uniqueId.m_address.equals(addr) && uniqueId.m_id == id) {
                return true;
            }
        }
        m_idCache.push(new UniqueId(addr, id, LocalTime.now().plus(lifeTime, ChronoUnit.MILLIS)));
        return false;
    }

    public int getSize() {
        purge();
        return m_idCache.size();
    }


    public int getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    private class UniqueId {
        InetSocketAddress m_address;
        int m_id;
        LocalTime m_expire;

        public UniqueId(InetSocketAddress m_address, int m_id, LocalTime m_expire) {
            this.m_address = m_address;
            this.m_id = m_id;
            this.m_expire = m_expire;
        }
    }

}
