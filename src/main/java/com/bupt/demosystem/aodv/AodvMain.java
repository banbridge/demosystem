package com.bupt.demosystem.aodv;

import com.bupt.demosystem.aodv.message.Message;
import com.bupt.demosystem.aodv.module.RouterNode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @Author banbridge
 * @Classname AODVMain
 * @Date 2021/5/28 14:18
 */
public class AodvMain {

    public static void main(String[] args) throws InterruptedException {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(10);
        Message ms = new Message();
        ms.setMessage("初始消息：" + UUID.randomUUID());
        int[] port = {9999, 9998};
        String ip = "127.0.0.1";
        InetSocketAddress fromAddress = new InetSocketAddress(ip, port[0]);
        InetSocketAddress toAddress = new InetSocketAddress(ip, port[1]);
        ms.setFromAddress(fromAddress);
        ms.setToAddress(toAddress);
        List<RouterNode> nodeList = new ArrayList<>();
        for (int i = 0; i < port.length; i++) {
            try {
                RouterNode rn = new RouterNode(i, ip, port[i], exec);
                nodeList.add(rn);
            } catch (IOException e) {
                System.out.println("创建节点" + i + "失败！！！！！");
                e.printStackTrace();
            }
        }
        RouterNode node1 = nodeList.get(0);
        node1.putUntreatedMessage(ms);
        nodeList.get(0).addConnectNode(nodeList.get(1));
        nodeList.get(1).addConnectNode(nodeList.get(0));
        for (RouterNode node : nodeList) {
            exec.submit(node);
        }
    }

}
