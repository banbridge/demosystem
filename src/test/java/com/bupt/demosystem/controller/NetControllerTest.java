package com.bupt.demosystem.controller;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.service.NetCreateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

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


    @Autowired
    ThreadPoolTaskScheduler scheduler;

    ScheduledFuture<?> task = null;

    @Test
    public void test2() {

        AtomicInteger count = new AtomicInteger();

        task =
                scheduler.scheduleAtFixedRate(() -> {
                    System.out.println(count.getAndIncrement());
                    if (count.get() > 10) {
                        task.cancel(true);
                    }
                }, 100);


    }

}
