package com.bupt.demosystem;

import com.bupt.demosystem.util.DRTD;
import com.bupt.demosystem.util.ShortPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

@SpringBootTest
class DemosystemApplicationTests {


    @Test
    void test2() {
        int n = 10;
        int[][] map = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                map[i][j] = (i == j ? 0 : ShortPath.MAX);
            }
        }
        map[0][1] = 1;
        map[0][2] = 2;
        map[0][3] = 3;
        map[0][4] = 4;
        map[0][5] = 5;
        map[1][6] = 5;
        map[2][6] = 4;
        map[3][6] = 3;
        map[4][6] = 2;
        map[5][6] = 1;
        map[6][7] = 1;
        map[6][8] = 3;
        map[6][9] = 5;
        map[8][9] = 2;
        map[7][9] = 4;
        ArrayList<LinkedList<Integer>> ans = ShortPath.multiPath(map, 0, 8);
        for (int i = 0; i < ans.size(); i++) {
            System.out.println(ans.get(i));
        }
    }

    @Test
    void testDRTD() {

        long startTime = System.currentTimeMillis();
        Random random = new Random();
        int n = 130;
        int cost[][] = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                cost[i][j] = (i == j ? 0 : random.nextInt(100));
            }

        }
        int MAX_COST = 40;
        DRTD drtd = new DRTD();
        drtd.setCosts(cost);
        drtd.setMAX_COST(MAX_COST);
        drtd.setN(n);

        int[][] map = drtd.getMap();
        for (int i = 0; i < n; i++) {
            System.out.println(Arrays.toString(map[i]));
        }
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
    }



}
