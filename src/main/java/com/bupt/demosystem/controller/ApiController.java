package com.bupt.demosystem.controller;

import com.bupt.demosystem.util.NetInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author banbridge
 * @Classname ApiController
 * @Date 2022/3/7 16:07
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    public NetInfo netInfo;

    @RequestMapping("getClusterPathList")
    public List getClusterPathList() {
        return netInfo.getPathData();
    }


}
