package com.bupt.demosystem.service;

import com.bupt.demosystem.entity.Cluster;
import com.bupt.demosystem.entity.Network;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author banbridge
 * @Classname NetClusterService
 * @Date 2022/4/26 15:15
 */
public interface NetClusterService {

    //根据网络拓扑的Id得到网络拓扑
    public Cluster getCluster(Integer id);

    //保存网络拓扑
    public Cluster saveCluster(Cluster network);

    //更新网络拓扑
    public Cluster updateCluster(Cluster network);

    //查询所有的网络拓扑图
    public ArrayList<Cluster> getAllCluster();

    //根据一定的规则分页查找网络拓扑

    /**
     * @param pageNum
     * @param pageSize
     * @return
     */
    public List<Cluster> getPageCluster(Integer pageNum, Integer pageSize);

    public boolean deleteNetByID(Integer id);

}
