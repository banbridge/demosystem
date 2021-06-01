package com.bupt.demosystem.controller;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.service.NetCreateService;
import com.bupt.demosystem.service.NetService;
import com.bupt.demosystem.util.NetInfo;
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
    NetService netService;

    private final Logger logger = LoggerFactory.getLogger(NetController.class);

    public NetController(NetCreateService netCreateService, NetService netService) {
        this.netCreateService = netCreateService;
        this.netService = netService;
    }

    @RequestMapping(value = "test")
    public String test() {
        return "test";
    }

    /**
     * 首页面
     *
     * @return
     */
    @RequestMapping("/")
    public ModelAndView homePage() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("vue-home");
        logger.info("homePage");
        return mv;
    }

    /**
     * 得到网络信息
     *
     * @return
     */
    @RequestMapping("/getNetInfo")
    @ResponseBody
    public Network getNetInfo() {
        return NetInfo.getNet();
    }

    /**
     * 根据传回的节点生成新的网络
     */
    @RequestMapping("/getNewNet")
    @ResponseBody
    public Network getNewNet(Integer num_node) {
        if (num_node == null) {
            num_node = 20;
        }
        Network net = netCreateService.getNetwork(num_node);
        NetInfo.setNetwork(net);
        logger.info("getNewNet");
        return NetInfo.getNet();
    }


    //得到多条最短路径，
    @RequestMapping("/getShortPath")
    @ResponseBody
    public ArrayList getShortPath(int start, int end) {
        //ArrayList<Integer> ans = new ArrayList<>();
        ArrayList<LinkedList<Integer>> path = ShortPath.multiPath(NetInfo.getNet(), start, end);

        logger.info("getShortPath");
        return path;
    }

    //从数据库加载网络
    @GetMapping(value = "/netWorkList")
    public String networkList(@RequestParam(value = "pageNumber", defaultValue = "1") String pageNumber,
                              @RequestParam(value = "pageSize", defaultValue = "10") String pageSize,
                              ModelMap modelMap) {
        List<Network> networks = netService.getPageNetwork(Integer.valueOf(pageNumber), Integer.valueOf(pageSize));
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
        Network net = NetInfo.getNet();
        Network netResult = null;
        logger.info("net value:" + net.getNetValue());
        try {
            if (net.getId() == -1) {
                netResult = netService.saveNetwork(net);
            } else {
                netResult = netService.updateNetWork(net);
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
    public boolean loadNewNet(int id) {
        logger.info("loadNewNet:" + id);
        Network net = null;

        try {
            net = netService.getNetwork(id);
            NetInfo.setNetwork(net);
        } catch (Exception e) {
            System.out.println("出现异常！！！！！！");
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
