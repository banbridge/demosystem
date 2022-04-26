package com.bupt.demosystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @Author banbridge
 * @Classname SettingConfig
 * @Date 2022/3/23 10:41
 */
@Component
@PropertySource(value = {"classpath:setting.yml"})
public class SettingConfig {
    private String test;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
