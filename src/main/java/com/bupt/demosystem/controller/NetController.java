package com.bupt.demosystem.controller;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.service.NetCreateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Created by Banbridge on 2020/12/30.
 */
@Controller
public class NetController {

    @Autowired
    NetCreateService netCreateService;

    @RequestMapping("/")
    public ModelAndView homePage() {

        ModelAndView mv = new ModelAndView();
        mv.setViewName("home");
        return mv;
    }

    @RequestMapping("/upgradeNet")
    @ResponseBody
    public Network upgradeNet(Integer num_node) {
        if (num_node == null) num_node = 20;
        Network net = netCreateService.getNetwork(num_node);
        return net;
    }


    @RequestMapping("/test")
    public String test() {
        return "test";
    }


}
