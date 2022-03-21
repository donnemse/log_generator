package com.yuganji.generator.queue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.yuganji.generator.model.EpsVO;

import lombok.Getter;

public class QueueObject {
    
    @Getter
    private LinkedBlockingQueue<Map<String, Object>> queue;
    
    @Getter
    private EpsVO consumerEps;
    
    @Getter
    private Map<Integer, EpsVO> producerEps;

    public QueueObject(int maxQueueSize) {
        this.queue = new LinkedBlockingQueue<>(maxQueueSize);
        this.consumerEps = new EpsVO(null);
        this.producerEps = new ConcurrentHashMap<>();
    }
}
