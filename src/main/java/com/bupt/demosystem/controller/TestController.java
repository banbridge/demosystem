package com.bupt.demosystem.controller;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.service.NetCreateService;
import com.bupt.demosystem.util.NetInfo;
import com.bupt.demosystem.util.NetUtil;
import com.bupt.demosystem.util.ShortPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
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

    // 简单统计，后续需要添加到各个节点里边
    public static int[][] map = null;
    public static CountTransmit1 countTransmit;

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
        map = NetUtil.getMapFromNetWork(netInfo.getNet());
        countTransmit = new CountTransmit1(map.length);
        netInfo.setPathData(null);
    }

    @GetMapping("getAllNets")
    public List<Network> getNetList() {
        netInfo.initNetInfo();
        return netInfo.getAllNet();
    }

    @RequestMapping("destroyNode")
    public boolean destroyNode(Integer c_id, Integer n_id) {
        for (int i = 0; i < map.length; i++) {
            if (i != n_id) {
                map[i][n_id] = map[n_id][i] = ShortPath.MAX;
            }
        }
        logger.info("delete Node : " + n_id);

        return netInfo.destroyNode(c_id, n_id);
    }

    //得到多条最短路径，
    @RequestMapping(value = {"/getShortPath"})
    public List getShortPath(int start, int end) {

        LinkedList<Integer> path = ShortPath.onePath(map, start, end);
        //logger.info("PathSIze: " + path.size());
        countTransmit.increaseSend(start);
        logger.info(String.format("start: %d, end: %d %d", start, end, path.size()));
        if (path.size() > 1) {
            countTransmit.increaseRecv(start);
        }

        return path;
    }

    //得到各个节点的业务传输成功情况
    @RequestMapping("/getCountTransmit")
    public CountTransmit1 getCountTransmit() {
        return countTransmit;
    }


    @RequestMapping("/startNodeRun")
    public boolean startNodeRun() {
        try {
            futureTask =
                    threadPoolTaskScheduler.scheduleAtFixedRate(() -> {
                        boolean isMove = netInfo.moveNodeCluster();
                        if (!isMove && futureTask != null) {
                            futureTask.cancel(true);
                        }
                        simpMessagingTemplate.convertAndSend("/all/greeting", netInfo.getAllNet());
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


//    @Scheduled(cron = "*/5 * * * * ?")
//    public void report() throws JsonProcessingException {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        String message = objectMapper.writeValueAsString(NetInfo.getAllNet());
//        logger.info("The time is now {}", new Date());
//        simpMessagingTemplate.convertAndSend("/all/greeting", message);
//    }


}


class CountTransmit1 {
    private int send[];
    private int recv[];
    private int sendSum;
    private int recvSum;

    public CountTransmit1(int n) {
        this.send = new int[n];
        this.recv = new int[n];
        sendSum = 0;
        recvSum = 0;
    }

    public void increaseSend(int index) {
        send[index]++;
        sendSum++;
    }

    public void increaseRecv(int index) {
        recv[index]++;
        recvSum++;
    }

    public int[] getSend() {
        return send;
    }

    public int[] getRecv() {
        return recv;
    }

    public int getSendSum() {
        return sendSum;
    }

    public int getRecvSum() {
        return recvSum;
    }
}