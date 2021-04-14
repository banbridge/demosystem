package com.bupt.demosystem.service;

import com.bupt.demosystem.entity.Network;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Banbridge on 2021/3/26.
 */
public interface NetService {

    //根据网络拓扑的Id得到网络拓扑
    public Network getNetwork(Integer id);

    //保存网络拓扑
    public Network saveNetwork(Network network);

    //更新网络拓扑
    public Network updateNetWork(Network network);

    //查询所有的网络拓扑图
    public ArrayList<Network> getAllNetwork();

    //根据一定的规则分页查找网络拓扑

    /**
     * @param pageNum
     * @param pageSize
     * @return
     */
    public List<Network> getPageNetwork(Integer pageNum, Integer pageSize);

    public boolean deleteNetByID(Integer id);

}
