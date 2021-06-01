package com.bupt.demosystem.util;

import com.bupt.demosystem.entity.Edge;
import com.bupt.demosystem.entity.Network;
import org.springframework.data.relational.core.sql.In;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by Banbridge on 2021/3/25.
 */
public class NetUtil {

    public static int[][] getMapFromNetWork(Network network) {
        int n = network.getNodeList().size();
        int[][] map = new int[n][n];
        for (Edge edge : network.getEdgeList()) {
            map[edge.getFrom()][edge.getTo()] = map[edge.getTo()][edge.getFrom()] = 1;
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && map[i][j] != 1) {
                    map[i][j] = ShortPath.MAX;
                }
            }
        }
        return map;
    }

    // 得到各个节点的抗毁度
    public static List<Double> getInvulnerability(int[][] map) {
        int n = map.length;
        List<Double> ans = new ArrayList<>();
        double ans_val[] = new double[n];
        int[] degree = new int[n];
        int[] count = new int[n];
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if (map[i][j] == 1) {
                    degree[i]++;
                    degree[j]++;
                }
                Arrays.fill(count, 0);
                ArrayList<LinkedList<Integer>> paths = ShortPath.multiPath(map, i, j);
                if (paths.size() < 1) {
                    continue;
                }
                for (int i_path = 0; i_path < paths.size(); i_path++) {
                    for (Integer num : paths.get(i_path)) {
                        count[num]++;
                    }
                }
                for (int k = 0; k < n; k++) {
                    ans_val[k] += count[k] / paths.size();
                }

            }
        }
        for (int i = 0; i < n; i++) {
            int sum = 0;
            for (int j = 0; j < n; j++) {
                if (j != i && map[i][j] == 1) {
                    sum += degree[j];
                }
            }
            if (sum == 0) {
                ans_val[i] = 0;
            } else {
                ans_val[i] *= (1.0 / ((n - 1) * (n - 2)) * (degree[i] * 1.0 / sum));
            }

        }
        for (int i = 0; i < n; i++) {
            ans.add(ans_val[i]);
        }
        return ans;
    }

    //得到一个节点的抗毁度
    public static Double getOneInvulnerability(int k, int[][] map) {
        double ans = 0;
        return ans;
    }

}
