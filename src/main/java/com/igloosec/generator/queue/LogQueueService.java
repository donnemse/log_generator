package com.igloosec.generator.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

@Service
public class LogQueueService {
    
    private Map<Integer, Queue<Map<String, Object>>> queue;
    
    @PostConstruct
    private void init() {
        this.queue = new HashMap<>();
    }
    
    public void newQueue(int port) {
        this.queue.put(port, new ConcurrentLinkedQueue<>());
    }
    
    public void removeQueue(int port) {
        this.queue.put(port, new ConcurrentLinkedQueue<>());
    }
    
    public Queue<Map<String, Object>> getQueue(int port) {
        if (!this.queue.containsKey(port)) {
            this.newQueue(port);
        }
        return this.queue.get(port);
    }
    
    public void pushLog(Map<String, Object> vo) {
        for (Entry<Integer, Queue<Map<String, Object>>> entry: queue.entrySet()) {
            entry.getValue().add(vo);
        }
    }
}
