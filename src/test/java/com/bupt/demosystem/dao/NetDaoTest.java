package com.bupt.demosystem.dao;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.entity.Node;
import com.bupt.demosystem.service.NetCreateService;
import com.bupt.demosystem.service.NetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Banbridge on 2021/3/25.
 */
@SpringBootTest
public class NetDaoTest {

    @Autowired
    NetCreateService netCreateService;

    @Resource
    NetService netService;

    @Test
    public void testNetDao() {
        Network network = netService.getNetwork(5);
        System.out.println(network.getNodeList().size());
    }

    @Test
    public void testNetInvulnerability() {
        Network net = netCreateService.getNetwork(30);
        List<Node> nodeList = net.getNodeList();
        for (Node node : nodeList) {
            System.out.println(node.getInvulnerability());
        }
    }


}
