package com.bupt.demosystem.controller;

import com.bupt.demosystem.service.NetCreateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;

/**
 * @author Banbridge
 * @date 2021/2/6
 */

@Controller()
public class AnalyseController {

    public final SimpMessagingTemplate simpMessagingTemplate;

    public final NetCreateService netCreateService;


    public AnalyseController(SimpMessagingTemplate simpMessagingTemplate, NetCreateService netCreateService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.netCreateService = netCreateService;
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Logger logger = LoggerFactory.getLogger(AnalyseController.class);

    @RequestMapping("/analyse")
    public String home() {
        return "analyse";
    }

    @MessageMapping("/hellostomp")
    @SendTo("/all/greeting")
    public String sayHello(String message) throws InterruptedException {
        logger.info("来自客户端的消息：" + message);
        return message;
    }

//    @Scheduled(cron = "*/5 * * * * ?")
//    public void report() throws JsonProcessingException {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        //String message = objectMapper.writeValueAsString(NetInfo.getSelectNetIndex());
//        //logger.info("The time is now {}", new Date());
//        //simpMessagingTemplate.convertAndSend("/all/greeting", message);
//    }

}
