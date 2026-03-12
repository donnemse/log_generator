package com.yuganji.generator.model;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class EpsVO {
    private String name;
    private volatile long lastCheckTime;
    private AtomicInteger cnt = new AtomicInteger(0);
    private volatile double eps;
    @JsonIgnore
    private transient AtomicInteger del = new AtomicInteger(0);
    private volatile double delEps;
    private long startedTime;
    private long runningTime;
    @JsonIgnore
    private transient Queue<EpsHistoryVO> epsHistory;

    public EpsVO(String name) {
        this.name = name;
        this.startedTime = System.currentTimeMillis();
        this.lastCheckTime = startedTime;
        this.epsHistory = new LinkedBlockingQueue<>(200);
        this.cnt = new AtomicInteger(0);
        this.del = new AtomicInteger(0);
    }

    public long getRunningTime() {
        this.runningTime = System.currentTimeMillis() - this.startedTime;
        return runningTime;
    }

    public void addDeleted() {
        this.del.incrementAndGet();
    }

    public void addDeleted(int count) {
        this.del.addAndGet(count);
    }

    public void addCnt() {
        this.cnt.incrementAndGet();
    }

    public void addCnt(int cnt) {
        this.cnt.addAndGet(cnt);
    }

    public void setEps(long time) {
        double seconds = (time - this.lastCheckTime) / 1000.0;
        if (seconds < 0.1) {
            // Sub-100ms window: don't reset counters, accumulate until next call
            return;
        }
        int cntSnapshot = this.cnt.getAndSet(0);
        int delSnapshot = this.del.getAndSet(0);
        this.eps = Math.round(cntSnapshot / seconds);
        this.delEps = Math.round(delSnapshot / seconds);
        this.lastCheckTime = time;
        if (this.epsHistory.size() == 200) {
            this.epsHistory.poll();
        }
        this.epsHistory.offer(new EpsHistoryVO(time, eps, delEps));
    }
}
