package com.bupt.demosystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author banbridge
 */
@SpringBootApplication
@EnableScheduling
public class DemosystemApplication {

    public Logger logger = LoggerFactory.getLogger(DemosystemApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DemosystemApplication.class, args);
    }

}
