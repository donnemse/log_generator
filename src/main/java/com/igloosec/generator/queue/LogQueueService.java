package com.igloosec.generator.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
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
    private Map<Integer, Map<Integer, EpsVO>> producerEpsCache;
    
    @Getter
    private Map<Integer, EpsVO> consumerEpsCache;
    
    @PostConstruct
    private void init() {
        this.queue = new HashMap<>();
        this.producerEpsCache = new ConcurrentHashMap<>();
        this.consumerEpsCache = new ConcurrentHashMap<>();
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
    
    public void push(Map<String, Object> vo, int loggerId) {
        long time = System.currentTimeMillis();
        for (Entry<Integer, Queue<Map<String, Object>>> entry: queue.entrySet()) {
            entry.getValue().offer(vo);
            
            int port = entry.getKey();
            if (!producerEpsCache.containsKey(port)) {
                producerEpsCache.put(port, new HashMap<>());
            }
            
            if (!producerEpsCache.get(port).containsKey(loggerId)) {
                EpsVO epsVO = new EpsVO();
                epsVO.setProducerLastCheckTime(time);
                producerEpsCache.get(port).put(loggerId, epsVO);
            }
            producerEpsCache.get(port).get(loggerId).addProducerCnt();
            long diff = time - producerEpsCache.get(port).get(loggerId).getProducerLastCheckTime();
            if (diff > 5 * 1000) {
                producerEpsCache.get(port).get(loggerId).setProducerEps(
                        producerEpsCache.get(port).get(loggerId).getProducerCnt() / (diff / 1000.d));
                producerEpsCache.get(port).get(loggerId).setProducerCnt(0);
                producerEpsCache.get(port).get(loggerId).setProducerLastCheckTime(time);
            }
        }
    }

    public List<Map<String, Object>> poll(int port, int maxBuffer) {
        long time = System.currentTimeMillis();
        
        if (!consumerEpsCache.containsKey(port)) {
            EpsVO epsVO = new EpsVO();
            epsVO.setConsumerLastCheckTime(time);
            consumerEpsCache.put(port, epsVO);
        }
        
        List<Map<String, Object>> list = new ArrayList<>();
        while(!queue.get(port).isEmpty() && list.size() < maxBuffer) {
            list.add(queue.get(port).poll());
            consumerEpsCache.get(port).addConsumerCnt();
        }
        long diff = time - consumerEpsCache.get(port).getConsumerLastCheckTime();
        if (diff > 5 * 1000) {
            consumerEpsCache.get(port).setConsumerEps(
                    consumerEpsCache.get(port).getConsumerCnt() / (diff / 1000.d));
            consumerEpsCache.get(port).setConsumerCnt(0);
            consumerEpsCache.get(port).setConsumerLastCheckTime (time);
        }
        return list;
    }
}
