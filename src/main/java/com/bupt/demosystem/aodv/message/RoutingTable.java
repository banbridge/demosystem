package com.bupt.demosystem.aodv.message;

import com.bupt.demosystem.aodv.message.tool.RouterFlags;

import java.net.InetSocketAddress;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @Author banbridge
 * @Classname RoutingTable
 * @Date 2021/6/10 18:32
 * 路由表类，每个节点维护一个路由表
 */
public class RoutingTable {

    /**
     * The routing table
     */
    private final HashMap<InetSocketAddress, RoutingTableEntry> ipAddressEntry;

    /**
     * Deletion time for invalid routes
     */
    private int m_badLinkLifetime;

    public RoutingTable(int m_badLinkLifetime) {
        this.ipAddressEntry = new HashMap<>();
        this.m_badLinkLifetime = m_badLinkLifetime;
    }

    /**
     * Add routing table entry if it doesn't yet exist in routing table
     * \param r routing table entry
     * \return true in success
     */
    public boolean AddRoute(RoutingTableEntry r) {
        purge();
        if (r.getFlag() != RouterFlags.IN_SEARCH) {
            r.setReqCount(0);
        }
        return ipAddressEntry.put(r.getDestIpAddress(), r) == null;
    }

    /**
     * Delete routing table entry with destination address dst, if it exists.
     * \param dst destination address
     * \return true on success
     */
    public boolean DeleteRoute(InetSocketAddress dst) {
        purge();
        return ipAddressEntry.remove(dst) == null;
    }

    /**
     * Lookup routing table entry with destination address dst
     * \param dst destination address
     * \param rt entry with destination address dst, if exists
     * \return true on success
     */
    public boolean lookupRoute(InetSocketAddress dst, RoutingTableEntry rt) {
        purge();
        if (ipAddressEntry.isEmpty()) {
            System.out.println("路由表为空");
            return false;
        }
        RoutingTableEntry rt_1 = ipAddressEntry.get(dst);
        if (rt_1 == null) {
            System.out.println("Route to " + dst + " not found!!");
            return false;
        }

        return true;
    }

    /**
     * Lookup route in VALID state
     * \param dst destination address
     * \param rt entry with destination address dst, if exists
     * \return true on success
     */
    public boolean lookupValidRoute(InetSocketAddress dst, RoutingTableEntry rt) {
        if (!lookupRoute(dst, rt)) {
            return false;
        }
        return rt.getFlag() == RouterFlags.VALID;
    }

    /**
     * Update routing table
     * \param rt entry with destination address dst, if exists
     * \return true on success
     */
    public boolean update(RoutingTableEntry rt) {
        if (!ipAddressEntry.containsKey(rt.getDestIpAddress())) {
            System.out.println(" Route update to " + rt.getDestIpAddress() + " fails; not found");
            return false;
        }

        if (rt.getFlag() != RouterFlags.IN_SEARCH) {
            rt.setReqCount(0);
            ipAddressEntry.put(rt.getDestIpAddress(), rt);
        }
        return true;
    }

    /**
     * Set routing table entry flags
     * \param dst destination address
     * \param state the routing flags
     * \return true on success
     */
    public boolean setEntryState(InetSocketAddress dst, RouterFlags state) {

        RoutingTableEntry entry = ipAddressEntry.get(dst);
        if (entry == null) {
            return false;
        }
        entry.setFlag(state);
        entry.setReqCount(0);
        return true;
    }

    /**
     * Lookup routing entries with next hop Address dst and not empty list of precursors.
     * <p>
     * \param nextHop the next hop IP address
     * \param unreachable
     */
    public void getListOfDestinationWithNextHop(InetSocketAddress nextHop, HashMap<InetSocketAddress, Integer> unreachable) {
        purge();
        unreachable.clear();
        for (Map.Entry<InetSocketAddress, RoutingTableEntry> entry : ipAddressEntry.entrySet()) {
            if (entry.getValue().getNextHop() == nextHop) {
                unreachable.put(entry.getKey(), entry.getValue().getDestSequenceNumber());
            }
        }
    }

    /**
     * Update routing entries with this destination as follows:
     * 1. The destination sequence number of this routing entry, if it
     * exists and is valid, is incremented.
     * 2. The entry is invalidated by marking the route entry as invalid
     * 3. The Lifetime field is updated to current time plus DELETE_PERIOD.
     * \param unreachable routes to invalidate
     */
    public void invalidateRoutesWithDst(HashMap<InetSocketAddress, Integer> unreachable) {
        purge();
        for (InetSocketAddress id : unreachable.keySet()) {
            RoutingTableEntry rte = ipAddressEntry.get(id);
            if (rte.getFlag() == RouterFlags.VALID) {
                rte.Invalidate(m_badLinkLifetime);
            }
        }
    }
    /**
     * Delete all route from interface with address iface
     * \param iface the interface IP address
     */
//    public void deleteAllRoutesFromInterface(InetSocketAddress iface){
//        if(ipAddressEntry.isEmpty()) {
//            return;
//        }
//        ipAddressEntry.entrySet().removeIf(entry -> entry.getValue().getInterFace()==iface);
//
//    }

    /**
     * Delete all entries from routing table
     */
    public void clear() {
        this.ipAddressEntry.clear();
    }

    /**
     * Delete all outdated entries and invalidate valid entry if Lifetime is expired
     */
    public void purge() {
        if (ipAddressEntry.isEmpty()) {
            return;
        }
        ipAddressEntry.entrySet().removeIf((entry) -> {
            if (entry.getValue().getLifeTime().isBefore(LocalTime.now())) {
                if (entry.getValue().getFlag() == RouterFlags.INVALID) {
                    return true;
                } else if (entry.getValue().getFlag() == RouterFlags.VALID) {
                    RoutingTableEntry rt = entry.getValue();
                    rt.Invalidate(m_badLinkLifetime);
                    entry.setValue(rt);
                }
            }
            return false;
        });
    }

    /**
     * Mark entry as unidirectional (e.g. add this neighbor to "blacklist" for blacklistTimeout period)
     * \param neighbor - neighbor address link to which assumed to be unidirectional
     * \param blacklistTimeout - time for which the neighboring node is put into the blacklist
     * \return true on success
     */
    public boolean markLinkAsUnidirectional(InetSocketAddress neighbor, int blacklistTimeout) {
        RoutingTableEntry rt = ipAddressEntry.get(neighbor);
        if (rt == null) {
            return false;
        }
        rt.setReqCount(0);
        return true;
    }


    public int getM_badLinkLifetime() {
        return m_badLinkLifetime;
    }

    public void setM_badLinkLifetime(int m_badLinkLifetime) {
        this.m_badLinkLifetime = m_badLinkLifetime;
    }
}

