package com.bupt.demosystem.controller;

import com.bupt.demosystem.config.SettingConfig;
import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.service.NetCreateService;
import com.bupt.demosystem.util.CountTransmit;
import com.bupt.demosystem.util.EasyExcelFileUtil;
import com.bupt.demosystem.util.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
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

    @Value("${file.filename}")
    private String fileName;

    public final SettingConfig setting;

    public final SimpMessagingTemplate simpMessagingTemplate;

    public final NetCreateService netCreateService;

    public final Group group;

    public final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private final Logger logger = LoggerFactory.getLogger(GroupController.class);

    private ScheduledFuture<?> futureTask;

    public GroupController(SettingConfig setting, SimpMessagingTemplate simpMessagingTemplate,
                           NetCreateService netCreateService,
                           Group group,
                           ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.setting = setting;
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
    public boolean destroyNode(Integer n_id) {
        logger.info("delete Node : " + n_id);
        return group.destroyNode(n_id);
    }

    //得到多条最短路径，
    @RequestMapping(value = {"/getShortPath"})
    public List getShortPath(int start, int end) {

        return group.getPathCount(start, end);
    }

    //得到各个节点的业务传输成功情况
    @RequestMapping("/getCountTransmit")
    public CountTransmit getCountTransmit() {
        return group.getCountTransmit();
    }

    @RequestMapping("/startNodeRun")
    public boolean startNodeRun() {

        try {
            futureTask =
                    threadPoolTaskScheduler.scheduleAtFixedRate(() -> {
                        boolean isMove = group.moveNodeCluster();
                        List<Network> network = group.getAllNetWork();

                        if (!isMove && futureTask != null) {
                            futureTask.cancel(true);
                            simpMessagingTemplate.convertAndSend("/all/greeting", "stop");
                        } else {
                            simpMessagingTemplate.convertAndSend("/all/greeting", network);
                        }

                    }, 1000);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @RequestMapping("/getClusterPathList")
    public List getClusterPathList() {
        return group.getPathData();
    }

    @RequestMapping("/endNodeRun")
    public boolean endNodeRun() {
        if (futureTask != null) {
            futureTask.cancel(true);
            return true;
        }
        return false;
    }

    @RequestMapping("/download")
    public String fielDownLoad(HttpServletResponse response) {
        File file = new File(EasyExcelFileUtil.getPath() + fileName);
        logger.info("-------" + file.getAbsolutePath());
        if (!file.exists()) {
            return "下载文件不存在";
        }
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             OutputStream os = response.getOutputStream();) {
            byte[] buff = new byte[1024];
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }

        } catch (IOException e) {
            logger.error("{}", e);
            return "下载失败";
        }
        return "下载成功";
    }

    @RequestMapping("/test")
    public String getTest() {
        return setting.toString();
    }


}
