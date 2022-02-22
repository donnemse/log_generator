package com.igloosec.generator.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.igloosec.generator.service.socket.EpsVO;

import lombok.Getter;

@Service
public class LogQueueService {
    private final int DEFAULT_QUEUE_MAX = 10_000;
    
    @Getter
    private Map<Integer, Queue<Map<String, Object>>> queue;
    
    @Getter
    private Map<Integer, EpsVO> epsCache;
    
    @PostConstruct
    private void init() {
        this.queue = new HashMap<>();
        this.epsCache = new HashMap<>();
    }
    
    public void newQueue(int port, int queueSize) {
        if (queueSize == 0) {
            queueSize = DEFAULT_QUEUE_MAX;
        }
        this.queue.put(port, new LinkedBlockingQueue<>(queueSize));
    }
    
    public void removeQueue(int port) {
        this.queue.remove(port);
    }
    
    public Queue<Map<String, Object>> getQueue(int port, int queueSize) {
        if (!this.queue.containsKey(port)) {
            this.newQueue(port, queueSize);
        }
        return this.queue.get(port);
    }
    
    public void push(Map<String, Object> vo) {
        long time = System.currentTimeMillis();
        for (Entry<Integer, Queue<Map<String, Object>>> entry: queue.entrySet()) {
            entry.getValue().offer(vo);
            
            if (!epsCache.containsKey(entry.getKey())) {
                EpsVO epsVO = new EpsVO();
                epsVO.setProducerLastCheckTime(time);
                epsCache.put(entry.getKey(), epsVO);
            }
            epsCache.get(entry.getKey()).addProducerCnt();
            long diff = time - epsCache.get(entry.getKey()).getProducerLastCheckTime();
            if (diff > 5 * 1000) {
                epsCache.get(entry.getKey()).setProducerEps(
                        epsCache.get(entry.getKey()).getProducerCnt() / (diff / 1000.d));
                epsCache.get(entry.getKey()).setProducerEps(0);
                epsCache.get(entry.getKey()).setProducerLastCheckTime(time);
            }
        }
    }

    public List<Map<String, Object>> poll(int port, int maxBuffer) {
        long time = System.currentTimeMillis();
        
        if (!epsCache.containsKey(port)) {
            EpsVO epsVO = new EpsVO();
            epsVO.setConsumerLastCheckTime(time);
            epsCache.put(port, epsVO);
        }
        
        List<Map<String, Object>> list = new ArrayList<>();
        while(!queue.get(port).isEmpty() && list.size() < maxBuffer) {
            list.add(queue.get(port).poll());
            epsCache.get(port).addConsumerCnt();
        }
        long diff = time - epsCache.get(port).getConsumerLastCheckTime();
        if (diff > 5 * 1000) {
            epsCache.get(port).setConsumerEps(
                    epsCache.get(port).getConsumerCnt() / (diff / 1000.d));
            epsCache.get(port).setConsumerEps(0);
            epsCache.get(port).setConsumerLastCheckTime (time);
        }
        return list;
    }
}
