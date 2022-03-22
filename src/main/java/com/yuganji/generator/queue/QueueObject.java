package com.yuganji.generator.queue;

import com.yuganji.generator.model.EpsVO;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueObject {
    
    @Getter
    private final LinkedBlockingQueue<Map<String, Object>> queue;
    
    @Getter
    private final EpsVO consumerEps;
    
    @Getter
    private final Map<Integer, EpsVO> producerEps;

    public QueueObject(int maxQueueSize) {
        this.queue = new LinkedBlockingQueue<>(maxQueueSize);
        this.consumerEps = new EpsVO(null);
        this.producerEps = new ConcurrentHashMap<>();
    }

    public void clearQueue() {
        this.queue.clear();
    }
}
