package com.igloosec.generator.restful.model;

import lombok.Data;

@Data
public class EpsVO {
    private String name;
    private long lastCheckTime;
    private int cnt;
    private double eps;
    private int deleted;
    private double deletedEps;
    private long startedTime;
    private long runningTime;
    
    public EpsVO() {
        this.startedTime = System.currentTimeMillis();
    }
    
    public long getRunningTime() {
        this.runningTime = System.currentTimeMillis() - this.startedTime;
        return runningTime;
    }
    
    public void addDeleted() {
        this.deleted++;
    }
    
    public void addCnt() {
        this.cnt++;
    }
    
    public void addCnt(int cnt) {
        this.cnt += cnt;
    }
}
