package com.bupt.demosystem.controller;

import com.alibaba.excel.EasyExcel;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
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
    public int destroyNode(Integer n_id) {
        logger.info("delete Node : " + n_id);
        Object[] ans = group.destroyNNodes(n_id);
        if (ans == null || ans.length < 1) {
            return -1;
        }
        return (int) ans[0];
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
                        int isMove = group.moveNodeCluster();

                        if (isMove == -2 && futureTask != null) {
                            futureTask.cancel(true);
                            simpMessagingTemplate.convertAndSend("/all/greeting", "stop");
                            group.initSimulatorStatus();
                            logger.info("仿真完成");
                            wirteToExcel(group.getAllCountTransmit());
                        } else {
                            if (isMove == -1) {
                                group.initSimulatorStatus();
                                simpMessagingTemplate.convertAndSend("/all/greeting", "init" + "," + group.getSizeOfDestroyedNode());
                                logger.info("开始第" + group.getSizeOfDestroyedNode() + "次仿真");
                            }
                            List<Network> network = group.getAllNetWork();
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
        //File file = new File(EasyExcelFileUtil.getPath() + fileName);
        File file = new File(fileName);
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

    private void wirteToExcel(ArrayList<CountTransmit> allCountTransmit) {
        //File file = new File(EasyExcelFileUtil.getPath() + fileName);
        File file = new File(fileName);
        logger.info("写入excel文件" + file.getAbsolutePath());
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            EasyExcel.write(file).head(head()).sheet("模板").doWrite(dataList(allCountTransmit));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<List<String>> head() {
        List<List<String>> list = new ArrayList<>();
        List<String> head0;
        String[] cols = {"节点总数", "故障率", "故障节点数", "总接收", "总发送", "总成功率", "簇内接收", "簇内发送", "簇内成功率", "簇间接收", "簇间发送", "簇间成功率"};
        for (String col : cols) {
            head0 = new ArrayList<>();
            head0.add(col);
            list.add(head0);
        }

        return list;
    }

    private List<List<Object>> dataList(ArrayList<CountTransmit> allCountTransmit) {
        List<List<Object>> list = new ArrayList<>();
        for (int i = 0; i < allCountTransmit.size(); i++) {
            List<Object> data = new ArrayList<>();
            CountTransmit c = allCountTransmit.get(i);
            data.add(c.getTotalNode());
            data.add(String.format("%.2f%%", c.getDestroyNode() * 100.0 / c.getTotalNode()));
            data.add(c.getDestroyNode());
            int[] recv = c.getRecvSum();
            int[] send = c.getSendSum();
            for (int j = 0; j < recv.length; j++) {
                data.add(recv[j]);
                data.add(send[j]);
                double rate = 0;
                if (send[j] != 0) {
                    rate = recv[j] * 100.0 / send[j];
                }
                data.add(String.format("%.2f%%", rate));
            }
            list.add(data);
        }
        return list;
    }

    @RequestMapping("/getSetting")
    public SettingConfig getSetting() {
        /**
         *     private int[] flyTime;
         *     private int[] maxHeight;
         *     private double[] endPos;
         *     private double[] startPos;
         *     private int speed;
         *     private int[] nodeSize;
         *     private double destroyRate;
         *     private int numOfBadNode;
         */
        SettingConfig sseting = new SettingConfig();
        sseting.setFlyTime(setting.getFlyTime());
        sseting.setMaxHeight(setting.getMaxHeight());
        sseting.setEndPos(setting.getEndPos());
        sseting.setStartPos(setting.getStartPos());
        sseting.setSpeed(setting.getSpeed());
        sseting.setNodeSize(setting.getNodeSize());
        sseting.setDestroyRate(setting.getDestroyRate());
        sseting.setNumOfBadNode(setting.getDestroyRate());

        return sseting;
    }

    @RequestMapping("/updateSetting")
    public SettingConfig updateSetting(@RequestBody SettingConfig sett) {
        /**
         *     private int[] flyTime;
         *     private int[] maxHeight;
         *     private double[] endPos;
         *     private double[] startPos;
         *     private int speed;
         *     private int[] nodeSize;
         *     private double destroyRate;
         *     private int numOfBadNode;
         */

        setting.setFlyTime(sett.getFlyTime());
        setting.setMaxHeight(sett.getMaxHeight());
        setting.setEndPos(sett.getEndPos());
        setting.setStartPos(sett.getStartPos());
        setting.setSpeed(sett.getSpeed());
        setting.setNodeSize(sett.getNodeSize());
        setting.setDestroyRate(sett.getDestroyRate());
        int[] nodeSize = setting.getNodeSize();
        int sum = 0;
        for (int ns : nodeSize) {
            sum += ns;
        }
        int count = sum * setting.getDestroyRate() / 100;
        setting.setNumOfBadNode(count);
        logger.info("修改配置为：" + setting.toString());
        group.initNetInfo();
        return getSetting();
    }

    @RequestMapping("/testSimulator")
    public ArrayList<CountTransmit> testSimulator() {
        ArrayList<CountTransmit> ans = group.simulatorTimes(20);
        wirteToExcel(ans);
        return ans;
    }


}
