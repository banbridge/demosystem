package com.bupt.demosystem.controller;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.service.NetCreateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by Banbridge on 2020/12/30.
 */
@SpringBootTest
@WebAppConfiguration
public class NetControllerTest {

    @Autowired
    NetCreateService netCreateService;

    @Test
    public void netCreateTest() {
        Network network = netCreateService.getNetwork();
        System.out.println(network.toString());
    }

}
