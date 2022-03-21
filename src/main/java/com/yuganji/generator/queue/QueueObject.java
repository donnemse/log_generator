package com.yuganji.generator.queue;

import com.yuganji.generator.model.EpsVO;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Data
public class QueueObject {
    private LinkedBlockingQueue<Map<String, Object>> queue;
    private Map<Integer, EpsVO> consumerEps;
    private Map<Integer, EpsVO> producerEps;

    public QueueObject(int maxQueueSize) {
        this.queue = new LinkedBlockingQueue<>();
        this.consumerEps = new ConcurrentHashMap<>();
        this.producerEps = new ConcurrentHashMap<>();
    }
}
