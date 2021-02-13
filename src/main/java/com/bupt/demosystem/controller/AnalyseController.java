package com.bupt.demosystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Banbridge on 2021/2/6.
 */

@Controller()
public class AnalyseController {

    @RequestMapping("/analyse")
    public String home() {
        return "analyse";
    }

}
