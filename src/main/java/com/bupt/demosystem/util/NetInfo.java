package com.bupt.demosystem.util;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.service.NetCreateService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Banbridge on 2021/2/21.
 */
public class NetInfo {

    private final int SIZEOFNET = 3;

    private static int selectIndex = 0;

    //簇的存储
    private static ArrayList<Network> nets = new ArrayList<>();
    static String ipBase = "192.168.";

    private static Network net;

    public static void setNetwork(Network network) {
        net = network;
    }

//    public static Network getNet() {
//        if (net == null) {
//            NetCreateService netCreateService = new NetCreateService();
//            net = netCreateService.getNetwork(20);
//        }
//        return net;
//    }

    public static void setSelectIndex(int index) {
        selectIndex = index;
    }

    public static Network getSelectNetIndex() {
        return nets.get(selectIndex);
    }

    public static void setSelectNet(Network net) {
        nets.set(selectIndex, net);
    }

    public static List<Network> getAllNet() {
        if (nets.size() < 1) {
            selectIndex = 0;
            NetCreateService netCreateService = new NetCreateService();
            Network net1 = netCreateService.getNetwork(18);
            nets.add(net1);
            Network net2 = netCreateService.getNetwork(20);
            nets.add(net2);
            Network net3 = netCreateService.getNetwork(15);
            nets.add(net3);
        }
        return nets;
    }


    public static Network getNetByIndex(Integer index) {
        if (index < nets.size()) {
            selectIndex = index;
            return nets.get(index);
        }
        return null;
    }

}
