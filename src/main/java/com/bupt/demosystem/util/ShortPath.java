package com.bupt.demosystem.util;

import com.bupt.demosystem.entity.Network;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Banbridge on 2021/1/26.
 */
public class ShortPath {


    public static final int MAX = 0x3f3f3f3f;

    //寻找从source到target的最短路
    public static LinkedList<Integer> onePath(int map[][], int source, int target) {
        LinkedList<Integer> path = new LinkedList<>();
        //狄杰斯特拉算法实现
        int n = map.length;
        int[] dis = new int[n];
        int[] pre = new int[n];
        boolean[] vis = new boolean[n];
        Arrays.fill(dis, MAX);
        Arrays.fill(pre, -1);
        dis[source] = 0;
        for (int i = 0; i < n; i++) {
            int k = -1;
            int min_d = MAX;
            for (int j = 0; j < n; j++) {
                if (!vis[j] && (k == -1 || dis[j] < min_d)) {
                    k = j;
                    min_d = dis[j];
                }
            }
            if (k == -1) {
                break;
            }
            vis[k] = true;
            for (int j = 0; j < n; j++) {
                if (!vis[j] && dis[k] + map[k][j] < dis[j]) {
                    dis[j] = dis[k] + map[k][j];
                    pre[j] = k;
                }
            }
        }
        int to = target;
        while (to != source && to != -1) {
            path.addFirst(to);
            to = pre[to];
        }
        path.addFirst(source);
        return path;
    }


    //network得到两点之间的最短路径，并按抗毁值从小到大排序
    public static ArrayList<LinkedList<Integer>> multiPath(Network network, int source, int target) {
        ArrayList<LinkedList<Integer>> paths = multiPath(NetUtil.getMapFromNetWork(network), source, target);
        ArrayList<PathSort> pathSorts = new ArrayList<>();
        paths.forEach(path -> {
            PathSort ps = new PathSort();
            double pathVale = 0;
            for (Integer id : path) {
                pathVale += network.getNodeList().get(id).getInvulnerability();
            }
            ;
            ps.setPathVale(pathVale);
            ps.setPath(path);
            pathSorts.add(ps);
        });
        Collections.sort(pathSorts);
        ArrayList<LinkedList<Integer>> ans = new ArrayList<>();
        pathSorts.forEach(ps -> {
            ans.add(ps.getPath());
        });
        return paths;
    }

    //Dijkstra算法 中的多邻接点与多条最短路径问题
    public static ArrayList<LinkedList<Integer>> multiPath(int[][] map, int source, int target) {
        ArrayList<LinkedList<Integer>> ans = new ArrayList<>();
        int n = map.length;
        //每个节点是否处理过
        boolean vis[] = new boolean[n];
        //其他节点到源节点的最短距离
        int[] dis = new int[n];
        //前置节点
        MultiArrayList[] preVertex = new MultiArrayList[n];
        //初始化数据
        for (int i = 0; i < n; i++) {
            vis[i] = false;
            dis[i] = map[source][i];
            preVertex[i] = new MultiArrayList();
        }
        dis[source] = 0;
        for (int i = 0; i < n; i++) {
            int k = -1;
            int min_d = MAX;
            for (int j = 0; j < n; j++) {
                if (!vis[j] && (k == -1 || dis[j] < min_d)) {
                    k = j;
                    min_d = dis[j];
                }
            }
            if (k == -1) {
                break;
            }
            vis[k] = true;
            for (int j = 0; j < n; j++) {
                if (!vis[j] && dis[k] + map[k][j] < dis[j]) {
                    dis[j] = dis[k] + map[k][j];
                    preVertex[j].arrayList.clear();
                    preVertex[j].arrayList.add(k);
                } else if (!vis[j] && dis[k] + map[k][j] == dis[j]) {
                    preVertex[j].arrayList.add(k);
                }
            }
        }
        if (preVertex[target].arrayList.size() > 0) {
            LinkedList<Integer> onePathList = new LinkedList<>();
            DFSPath(ans, preVertex, source, target, onePathList);
        }
        return ans;
    }

    private static void DFSPath(ArrayList<LinkedList<Integer>> ans, MultiArrayList[] preVertex, int source, int target, LinkedList<Integer> onePathList) {
        if (target == source) {
            LinkedList<Integer> copy = new LinkedList<>();
            copy.addAll(onePathList);
            copy.addFirst(source);
            ans.add(copy);
            return;
        }
        onePathList.addFirst(target);
        for (int i = 0; i < preVertex[target].arrayList.size(); i++) {
            int index = preVertex[target].arrayList.get(i);
            DFSPath(ans, preVertex, source, index, onePathList);
        }
        onePathList.removeFirst();
    }


}


class MultiArrayList {
    ArrayList<Integer> arrayList;

    public MultiArrayList() {
        this.arrayList = new ArrayList();
    }

}

class PathSort implements Comparable<PathSort> {
    private double pathVale;
    private LinkedList<Integer> path;

    @Override
    public int compareTo(PathSort o) {
        return (int) (this.pathVale - o.pathVale);
    }

    public double getPathVale() {
        return pathVale;
    }

    public void setPathVale(double pathVale) {
        this.pathVale = pathVale;
    }

    public LinkedList<Integer> getPath() {
        return path;
    }

    public void setPath(LinkedList<Integer> path) {
        this.path = path;
    }
}
