package com.bupt.demosystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Banbridge on 2021/3/25.
 */
@Entity
public class NetMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(columnDefinition = "double default 0")
    private double netvalue;

    public double getNetvalue() {
        return netvalue;
    }

    public void setNetvalue(double netvalue) {
        this.netvalue = netvalue;
    }

    @Column(nullable = false, columnDefinition = "text")
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String net;


    private String modifiedTime;

    public NetMemory(String net) {
        this.net = net;
    }

    public NetMemory() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNet() {
        return net;
    }

    public void setNet(String net) {
        this.net = net;
    }

    public String getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
