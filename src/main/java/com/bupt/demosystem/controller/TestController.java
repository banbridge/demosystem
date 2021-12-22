package com.bupt.demosystem.controller;

import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.service.NetCreateService;
import com.bupt.demosystem.util.NetInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

/**
 * @Author banbridge
 * @Classname TestController
 * @Date 2021/12/19 14:35
 */
@Controller
@RequestMapping("/test")
public class TestController {

    public final SimpMessagingTemplate simpMessagingTemplate;

    public final NetCreateService netCreateService;

    private final Logger logger = LoggerFactory.getLogger(TestController.class);

    public TestController(SimpMessagingTemplate simpMessagingTemplate, NetCreateService netCreateService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.netCreateService = netCreateService;
    }

    @RequestMapping(value = "")
    public String test() {
        return "vue-test";
    }

    @RequestMapping("getAllNets")
    @ResponseBody
    public List<Network> getNetList() {
        return NetInfo.getAllNet();
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
