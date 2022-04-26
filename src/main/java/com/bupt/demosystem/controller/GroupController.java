package com.bupt.demosystem.controller;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.service.NetCreateService;
import com.bupt.demosystem.util.CountTransmit;
import com.bupt.demosystem.util.Group;
import com.bupt.demosystem.util.NetInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * @Author banbridge
 * @Classname GroupController
 * @Date 2022/4/26 12:03
 */

@RestController
@RequestMapping("/group")
public class GroupController {

    public final SimpMessagingTemplate simpMessagingTemplate;

    public final NetCreateService netCreateService;

    public final Group group;

    public final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private final Logger logger = LoggerFactory.getLogger(TestController.class);

    private ScheduledFuture<?> futureTask;

    public GroupController(SimpMessagingTemplate simpMessagingTemplate,
                           NetCreateService netCreateService,
                           Group group,
                           ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.netCreateService = netCreateService;
        this.group = group;
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
        group.setPathData(null);
        group.initNetInfo();
        if (futureTask != null) {
            futureTask.cancel(true);
        }
    }

    @GetMapping("getAllNets")
    public List<Network> getNetList() {
        return group.getAllNetWork();
    }

    @RequestMapping("destroyNode")
    public boolean destroyNode(Integer c_id, Integer n_id) {

        logger.info("delete Node : " + n_id);
        return group.destroyNode(c_id, n_id);
    }

    //得到多条最短路径，
    @RequestMapping(value = {"/getShortPath"})
    public List getShortPath(int start, int end) {

        int c_i1 = start / 100;
        int c_i2 = end / 100;
        int n_i1 = start % 100;
        int n_i2 = end % 100;
        return group.getPath(c_i1, n_i1, c_i2, n_i2);
    }

    //得到各个节点的业务传输成功情况
    @RequestMapping("/getCountTransmit")
    public List<CountTransmit> getCountTransmit() {
        return group.getCountTransmit();
    }

    @RequestMapping("/startNodeRun")
    public boolean startNodeRun() {

        try {
            futureTask =
                    threadPoolTaskScheduler.scheduleAtFixedRate(() -> {
                        boolean isMove = group.moveNodeCluster();
                        simpMessagingTemplate.convertAndSend("/all/greeting", group.getAllNetWork());
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
