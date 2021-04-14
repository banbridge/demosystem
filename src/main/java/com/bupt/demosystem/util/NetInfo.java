package com.bupt.demosystem.util;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.service.NetCreateService;

/**
 * Created by Banbridge on 2021/2/21.
 */
public class NetInfo {


    private static Network net;

    public static void setNetwork(Network network) {
        net = network;
    }

    public static Network getNet() {
        if (net == null) {
            NetCreateService netCreateService = new NetCreateService();
            net = netCreateService.getNetwork(20);
        }
        return net;
    }


}
