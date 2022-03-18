package com.yuganji.generator.model;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class EpsVO {
    private String name;
    private long lastCheckTime;
    private int cnt;
    private double eps;
    @JsonIgnore
    private transient int del;
    private double delEps;
    private long startedTime;
    private long runningTime;
    @JsonIgnore
    private transient Queue<EpsHistoryVO> epsHistory;
    
    public EpsVO(String name) {
        this.name = name;
        this.startedTime = System.currentTimeMillis();
        this.lastCheckTime = startedTime;
        this.epsHistory = new LinkedBlockingQueue<>(200);
    }

    public long getRunningTime() {
        this.runningTime = System.currentTimeMillis() - this.startedTime;
        return runningTime;
    }
    
    public void addDeleted() {
        this.del++;
    }
    
    public void addCnt() {
        this.cnt++;
    }
    
    public void addCnt(int cnt) {
        this.cnt += cnt;
    }
    
    public void setEps(long time) {
        this.eps = Math.ceil(this.cnt / Math.floor((time - this.lastCheckTime) / 1000.d));
        this.delEps = Math.ceil(this.del / Math.floor((time - this.lastCheckTime) / 1000.d));
        this.cnt = 0;
        this.del = 0;
        this.lastCheckTime = time;
        if (this.epsHistory.size() == 200) {
            this.epsHistory.poll();
        }
        this.epsHistory.offer(new EpsHistoryVO(time, eps, delEps));
    }
}
