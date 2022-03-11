package com.yuganji.generator.model;

import lombok.Data;

@Data
public class IpLocationVO implements Comparable<Long> { 

    private String code;
    private String name;
    private long sip;
    private long eip;

    public IpLocationVO(String code, String name,long sip, long eip) {
        this.code = code;
        this.sip = sip;
        this.eip = eip;
        this.name = name;
    }

    public int compareTo(Long l) {
        if (l < sip) 
            return 1; 
        if (l >= sip && l <= eip)
            return 0;
        else 
            return -1;
    }
}
