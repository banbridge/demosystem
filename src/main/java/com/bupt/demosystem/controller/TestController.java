package com.bupt.demosystem.controller;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.service.NetCreateService;
import com.bupt.demosystem.util.CountTransmit;
import com.bupt.demosystem.util.NetInfo;
import com.bupt.demosystem.util.NetUtil;
import com.bupt.demosystem.util.ShortPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * @Author banbridge
 * @Classname TestController
 * @Date 2021/12/19 14:35
 */
@RestController
@RequestMapping("/test")
public class TestController {

    public final SimpMessagingTemplate simpMessagingTemplate;

    public final NetCreateService netCreateService;

    public final NetInfo netInfo;

    public final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private final Logger logger = LoggerFactory.getLogger(TestController.class);

    private ScheduledFuture<?> futureTask;

    public TestController(SimpMessagingTemplate simpMessagingTemplate,
                          NetCreateService netCreateService,
                          NetInfo netInfo,
                          ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.netCreateService = netCreateService;
        this.netInfo = netInfo;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    }


    @RequestMapping(value = "")
    public ModelAndView test() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vue-test");
        initCountMap();
        return modelAndView;
    }

    public void initCountMap() {
        netInfo.setPathData(null);
        netInfo.initNetInfo();
        if (futureTask != null) {
            futureTask.cancel(true);
        }
    }

    @GetMapping("getAllNets")
    public List<Network> getNetList() {
        return netInfo.getAllNet();
    }

    @RequestMapping("destroyNode")
    public boolean destroyNode(Integer c_id, Integer n_id) {

        logger.info("delete Node : " + n_id);

        return netInfo.destroyNode(c_id, n_id);
    }

    //得到多条最短路径，
    @RequestMapping(value = {"/getShortPath"})
    public List getShortPath(int start, int end) {

        int c_i1 = start / 100;
        int c_i2 = end / 100;
        int n_i1 = start % 100;
        int n_i2 = end % 100;
        return netInfo.getPath(c_i1, n_i1, c_i2, n_i2);
    }

    //得到各个节点的业务传输成功情况
    @RequestMapping("/getCountTransmit")
    public List<CountTransmit> getCountTransmit() {
        return netInfo.getCountTransmit();
    }


    @RequestMapping("/startNodeRun")
    public boolean startNodeRun() {

        try {
            futureTask =
                    threadPoolTaskScheduler.scheduleAtFixedRate(() -> {
                        boolean isMove = netInfo.moveNodeCluster();
                        simpMessagingTemplate.convertAndSend("/all/greeting", netInfo.getAllNet());
                        if (!isMove && futureTask != null) {
                            futureTask.cancel(true);
                        }

                    }, 1000);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    @RequestMapping("/endNodeRun")
    public boolean endNodeRun() {
        if (futureTask != null) {
            futureTask.cancel(true);
            return true;
        }
        return false;
    }




}