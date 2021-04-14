package com.bupt.demosystem.repository;

import com.bupt.demosystem.entity.NetMemory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by Banbridge on 2021/3/25.
 */
public interface NetRepository extends JpaRepository<NetMemory, Integer> {

    @Override
    public Optional<NetMemory> findById(Integer id);


}
