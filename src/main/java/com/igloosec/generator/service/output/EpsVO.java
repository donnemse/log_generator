package com.igloosec.generator.service.output;

import lombok.Data;

@Data
public class EpsVO {
    private String name;
    
    private long producerLastCheckTime;
    private long consumerLastCheckTime;
    private int producerCnt;
    private int consumerCnt;
    private double producerEps;
    private double consumerEps;
    
    public void addProducerCnt() {
        this.producerCnt += 1;
    }
    
    public void addConsumerCnt() {
        this.consumerCnt += 1;
    }
}
