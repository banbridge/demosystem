package com.bupt.demosystem.controller;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.service.NetClusterService;
import com.bupt.demosystem.service.NetCreateService;
import com.bupt.demosystem.service.NetService;
import com.bupt.demosystem.entity.Cluster;
import com.bupt.demosystem.util.Group;
import com.bupt.demosystem.util.ShortPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Banbridge on 2020/12/30.
 */
@Controller
public class NetController {

    final
    NetCreateService netCreateService;
    final
    NetClusterService netService;
    final
    Group group1;


    private final Logger logger = LoggerFactory.getLogger(NetController.class);

    public NetController(NetCreateService netCreateService, NetClusterService netService, Group group) {
        this.netCreateService = netCreateService;
        this.netService = netService;
        this.group1 = group;

    }

    /**
     * 首页面
     *
     * @return
     */
    @RequestMapping("/")
    public ModelAndView homePage() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("home");
        return mv;
    }

//    /**
//     * 得到网络信息
//     *
//     * @return
//     */
//    @RequestMapping("/getNetInfo")
//    @ResponseBody
//    public Network getNetInfo() {
//        return NetInfo.getNet();
//    }


    /**
     * 根据传回的节点生成新的网络
     */
    @RequestMapping("/getNewNet")
    @ResponseBody
    public Cluster getNewNet(Integer num_node) {
        if (num_node == null) {
            num_node = 20;
        }

        ArrayList<Network> net = netCreateService.getNetworkList(num_node);
        Cluster cls = new Cluster();
        cls.setCluster(net);
        group1.setSelectNet(cls);
        logger.info("getNewNet");
        return group1.getSelectNetIndex();
    }

    @RequestMapping("getNetList")
    @ResponseBody
    public ArrayList<Cluster> getNetList() {
        return group1.getAllNet();
    }

    @RequestMapping("getNetListSize")
    @ResponseBody
    public int getNetListSize() {
        group1.setSelectIndex(0);
        logger.info("getNetListSize");
        return group1.getAllNet().size();
    }

    @RequestMapping("/getNetByIndex")
    @ResponseBody
    public Cluster getNetByIndex(Integer index) {
        logger.info("请求的index：" + index);
        Cluster cls = group1.getNetByIndex(index);
        logger.info("请求的群簇的个数：" + cls.getCluster().size());
        return cls;
    }


    //得到多条最短路径，
    @RequestMapping(value = {"/getShortPath"})
    @ResponseBody
    public ArrayList getShortPath(int start, int end) {
        //ArrayList<LinkedList<Integer>> path = ShortPath.multiPathList(netInfo.getSelectNetIndex(), start, end);
        logger.info("getShortPath path.size:" + start + " " + end);
        ArrayList<LinkedList<Integer>> path = group1.getPath(start, end);
        //logger.info("getShortPath path.size:" + path.size());
        return path;
    }


    //得到多条最短路径，
    @RequestMapping(value = {"/getShortPathIndex0"})
    @ResponseBody
    public ArrayList getShortPathIndex0(int start, int end) {
        //ArrayList<LinkedList<Integer>> path = ShortPath.multiPathListBest(netInfo.getNet(), start, end);
        logger.info("getShortPath");
        return null;
    }

    //从数据库加载网络
    @GetMapping(value = "/netWorkList")
    public String networkList(@RequestParam(value = "pageNumber", defaultValue = "1") Integer pageNumber,
                              @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                              ModelMap modelMap) {
        List<Cluster> networks = netService.getPageCluster(pageNumber, pageSize);
        int total_pages = 1;
        int total_networks = 0;
        if (networks.size() > 0) {
            total_networks = (int) networks.remove(networks.size() - 1).getId();
            total_pages = (int) networks.remove(networks.size() - 1).getId();
        }
        logger.info("networkList:" + (networks.size()));
        modelMap.addAttribute("networks", networks);
        modelMap.addAttribute("pageNumber", pageNumber);
        modelMap.addAttribute("totalPages", total_pages);
        modelMap.addAttribute("totalNet", total_networks);
        return "fragments/commons :: #home_table";
    }

    //删除网络
    @GetMapping(value = "deleteNet")
    @ResponseBody
    public boolean deleteById(@RequestParam Integer id) {
        logger.info("deleteById");
        return netService.deleteNetByID(id);
    }


    //保存网络
    @RequestMapping(value = "/saveNet")
    @ResponseBody
    public boolean saveNet() {
        logger.info("saveNet");
        Cluster net = group1.getSelectNetIndex();
        Cluster netResult = null;
        logger.info("net value:" + net.getNetValue());
        try {
            if (net.getId() == -1) {
                netResult = netService.saveCluster(net);
            } else {
                netResult = netService.updateCluster(net);
            }
            if (netResult == null) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("出现异常，保存失败");
            return false;
        }

        return true;
    }

    // 将服务端更新成客户端加载的网络
    @RequestMapping(value = "loadNewNet")
    @ResponseBody
    public Cluster loadNewNet(int id) {
        logger.info("loadNewNet:" + id);
        Cluster net = null;
        try {
            net = netService.getCluster(id);
            group1.setSelectNet(net);
        } catch (Exception e) {
            System.out.println("出现异常！！！！！！");
            e.printStackTrace();
            return null;
        }

        return group1.getSelectNetIndex();
    }

}
