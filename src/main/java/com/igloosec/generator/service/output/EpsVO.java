package com.igloosec.generator.service.output;

import lombok.Data;

@Data
public class EpsVO {
    private String name;
    private long lastCheckTime;
    private int cnt;
    private double eps;
    private long startedTime;
    private long runningTime;
    
    public EpsVO() {
        this.startedTime = System.currentTimeMillis();
    }
    
    public long getRunningTime() {
        this.runningTime = System.currentTimeMillis() - this.startedTime;
        return runningTime;
    }
    
    
    public void addCnt() {
        this.cnt++;
    }
}
