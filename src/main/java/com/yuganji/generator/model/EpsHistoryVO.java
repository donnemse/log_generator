package com.yuganji.generator.model;

import lombok.Data;

@Data
public class EpsHistoryVO {
    private long time;
    private double eps;
    private double delEps;
    
    public EpsHistoryVO(long time, double eps, double delEps) {
        this.time = time;
        this.eps = eps;
        this.delEps = delEps;
    }
}
