package com.bupt.demosystem.service.iml;

import com.bupt.demosystem.entity.NetMemory;
import com.bupt.demosystem.entity.Network;
import com.bupt.demosystem.repository.NetRepository;
import com.bupt.demosystem.service.NetService;
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
 * Created by Banbridge on 2021/3/25.
 */
@Service
public class NetServiceImpl implements NetService {

    final
    NetRepository netRepository;
    final
    ObjectMapper objectMapper;

    public NetServiceImpl(NetRepository netRepository, ObjectMapper objectMapper) {
        this.netRepository = netRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Network getNetwork(Integer id) {
        Network network = null;
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
    public Network saveNetwork(Network network) {
        String m = null;
        Network ans = null;
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
    public Network updateNetWork(Network network) {
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
    public ArrayList<Network> getAllNetwork() {
        ArrayList<Network> ans = new ArrayList<>();

        List<NetMemory> netMemoryList = netRepository.findAll();
        for (NetMemory netMemory : netMemoryList) {
            try {
                Network temp = stringToNetwork(netMemory.getNet());
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
    public List<Network> getPageNetwork(Integer pageNum, Integer pageSize) {
        Page<NetMemory> netMemoryPage = netRepository.findAll(PageRequest.of(pageNum - 1, pageSize));
        ArrayList<Network> networks = new ArrayList<Network>();
        // System.out.println(pageNum+"------"+netMemoryPage.getContent().size());
        for (NetMemory netMemory : netMemoryPage.getContent()) {
            try {
                Network temp = stringToNetwork(netMemory.getNet());
                System.out.println("====" + netMemory.getNet());
                System.out.println("====" + temp.getNetValue());
                temp.setId(netMemory.getId());
                temp.setNetValue(netMemory.getNetvalue());
                temp.setModifiedTime(netMemory.getModifiedTime());
                networks.add(temp);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        Network net_total_network = new Network();
        net_total_network.setId((int) netMemoryPage.getTotalElements());
        Network net_total_page = new Network();
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

    private Network stringToNetwork(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Network.class);
    }

    private String networkToString(Network network) throws JsonProcessingException {
        return objectMapper.writeValueAsString(network);
    }


    private String dateToString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

}
