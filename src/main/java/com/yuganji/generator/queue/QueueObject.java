package com.yuganji.generator.queue;

import com.yuganji.generator.model.EpsVO;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueObject {
    
    @Getter
    private final LinkedBlockingQueue<Map<String, Object>> queue;
    
    @Getter
    private final EpsVO consumerEps;
    
    @Getter
    private final Map<Integer, EpsVO> producerEps;

    @Getter
    private Set<String> filter;

    public QueueObject(int maxQueueSize, String filter) {
        if (maxQueueSize == 0){
            maxQueueSize = 100_000;
        }
        if (filter != null){
            this.filter = new HashSet<>();
            Arrays.stream(filter.split(",")).forEach(x -> {
                if (x.trim().length() > 0)
                    this.filter.add(x.trim().toLowerCase());
            });
        }
        this.queue = new LinkedBlockingQueue<>(maxQueueSize);
        this.consumerEps = new EpsVO(null);
        this.producerEps = new ConcurrentHashMap<>();
    }

    public void clearQueue() {
        this.queue.clear();
    }
}
