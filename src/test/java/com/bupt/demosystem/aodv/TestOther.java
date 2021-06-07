package com.bupt.demosystem.aodv;

import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author banbridge
 * @Classname Testother
 * @Date 2021/6/7 09:44
 */
public class TestOther {

    @Test
    public void test1() {

    }

    private void printHashMap(HashMap<Integer, Object> hashMap) {

        for (Map.Entry entry : hashMap.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }

    }

}
