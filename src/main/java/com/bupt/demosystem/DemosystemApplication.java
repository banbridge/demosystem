package com.bupt.demosystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

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

    //token: 'pk.eyJ1IjoiYmFuYnJpZGdlIiwiYSI6ImNrbm9jbWZwODEyeWkyd3FqeWlrMjBpNDkifQ.IKOlC_ndL5W8Lpa_XAVmJA',


}
