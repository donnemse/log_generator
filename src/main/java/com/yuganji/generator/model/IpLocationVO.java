package com.yuganji.generator.model;

import lombok.Getter;

@Getter
public class IpLocationVO implements Comparable<Long> {

    private final String code;
    private final String name;
    private final long sip;
    private final long eip;

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
