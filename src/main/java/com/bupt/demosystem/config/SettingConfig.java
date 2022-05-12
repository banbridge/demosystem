package com.bupt.demosystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @Author banbridge
 * @Classname SettingConfig
 * @Date 2022/3/23 10:41
 */
@Component
@Configuration
@PropertySource(value = {"classpath:setting.properties"}, encoding = "UTF-8")
@ConfigurationProperties(prefix = "setting")
public class SettingConfig {
    private int[] flyTime;
    private int[] maxHeight;
    private double[] endPos;
    private double[] startPos;
    private int speed;
    private int[] nodeSize;


    public int[] getFlyTime() {
        return flyTime;
    }

    public void setFlyTime(int[] flyTime) {
        this.flyTime = flyTime;
    }

    public int[] getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int[] maxHeight) {
        this.maxHeight = maxHeight;
    }

    public double[] getEndPos() {
        return endPos;
    }

    public void setEndPos(double[] endPos) {
        this.endPos = endPos;
    }

    public double[] getStartPos() {
        return startPos;
    }

    public void setStartPos(double[] startPos) {
        this.startPos = startPos;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int[] getNodeSize() {
        return nodeSize;
    }

    public void setNodeSize(int[] nodeSize) {
        this.nodeSize = nodeSize;
    }

    @Override
    public String toString() {
        return "SettingConfig{" +
                "flyTime=" + Arrays.toString(flyTime) +
                ", maxHeight=" + Arrays.toString(maxHeight) +
                ", endPos=" + Arrays.toString(endPos) +
                ", startPos=" + Arrays.toString(startPos) +
                ", speed=" + speed +
                ", nodeSize=" + Arrays.toString(nodeSize) +
                '}';
    }
}
