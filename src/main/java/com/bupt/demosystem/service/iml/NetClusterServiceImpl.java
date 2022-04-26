package com.bupt.demosystem.service.iml;

import com.bupt.demosystem.entity.Cluster;
import com.bupt.demosystem.entity.NetMemory;
import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.repository.NetRepository;
import com.bupt.demosystem.service.NetClusterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author banbridge
 * @Classname NetClusterServiceImpl
 * @Date 2022/4/26 15:17
 */
@Service
public class NetClusterServiceImpl implements NetClusterService {

    final
    NetRepository netRepository;
    final
    ObjectMapper objectMapper;

    public NetClusterServiceImpl(NetRepository netRepository, ObjectMapper objectMapper) {
        this.netRepository = netRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Cluster getCluster(Integer id) {
        Cluster network = null;
        NetMemory netMemory = netRepository.findById(id).orElse(null);
        try {
            assert netMemory != null;
            network = stringToNetwork(netMemory.getNet());
            network.setId(netMemory.getId());
            network.setModifiedTime(netMemory.getModifiedTime());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return network;
    }

    @Override
    public Cluster saveCluster(Cluster network) {
        String m = null;
        Cluster ans = null;
        try {
            Date date = new Date();
            m = networkToString(network);
            NetMemory netMemory = new NetMemory(m);
            netMemory.setNetvalue(network.getNetValue());
            netMemory.setModifiedTime(dateToString(date));
            netRepository.save(netMemory);
            ans = network;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return ans;
    }

    @Override
    public Cluster updateCluster(Cluster network) {
        NetMemory netMemory = new NetMemory();
        netMemory.setId(network.getId());
        try {
            Date date = new Date();
            netMemory.setNetvalue(network.getNetValue());
            netMemory.setModifiedTime(dateToString(date));
            netMemory.setNet(networkToString(network));
            netRepository.save(netMemory);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return network;
    }

    @Override
    public ArrayList<Cluster> getAllCluster() {
        ArrayList<Cluster> ans = new ArrayList<>();

        List<NetMemory> netMemoryList = netRepository.findAll();
        for (NetMemory netMemory : netMemoryList) {
            try {
                Cluster temp = stringToNetwork(netMemory.getNet());
                temp.setId(netMemory.getId());
                temp.setModifiedTime(netMemory.getModifiedTime());
                ans.add(temp);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return ans;
    }

    @Override
    public List<Cluster> getPageCluster(Integer pageNum, Integer pageSize) {
        Page<NetMemory> netMemoryPage = netRepository.findAll(PageRequest.of(pageNum - 1, pageSize));
        ArrayList<Cluster> networks = new ArrayList<>();
        for (NetMemory netMemory : netMemoryPage.getContent()) {
            try {
                Cluster temp = stringToNetwork(netMemory.getNet());
                temp.setId(netMemory.getId());
                temp.setNetValue(netMemory.getNetvalue());
                temp.setModifiedTime(netMemory.getModifiedTime());
                networks.add(temp);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        Cluster net_total_network = new Cluster();
        net_total_network.setId((int) netMemoryPage.getTotalElements());
        Cluster net_total_page = new Cluster();
        net_total_page.setId(netMemoryPage.getTotalPages());
        networks.add(net_total_page);
        networks.add(net_total_network);

        return networks;
    }

    @Override
    public boolean deleteNetByID(Integer id) {
        try {
            netRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Cluster stringToNetwork(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Cluster.class);
    }

    private String networkToString(Cluster network) throws JsonProcessingException {
        return objectMapper.writeValueAsString(network);
    }


    private String dateToString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

}
