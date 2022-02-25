package com.igloosec.generator.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.igloosec.generator.prop.LoggerManager;
import com.igloosec.generator.restful.model.EpsVO;

import lombok.Getter;

@Service
public class QueueService {
    private final int DEFAULT_QUEUE_MAX = 10_000;
    
    @Getter
    private Map<Integer, LinkedBlockingQueue<Map<String, Object>>> queue;
    
    @Getter
    private Map<Integer, Map<Integer, EpsVO>> producerEpsCache;
    
    @Getter
    private Map<Integer, EpsVO> consumerEpsCache;
    
    @Autowired
    private LoggerManager loggerMgr;
    
    @PostConstruct
    private void init() {
        this.queue = new HashMap<>();
        this.producerEpsCache = new ConcurrentHashMap<>();
        this.consumerEpsCache = new ConcurrentHashMap<>();
    }
    
    @Scheduled(initialDelay = 3000, fixedDelay = 3000)
    public void monitorEps() {
        long time = System.currentTimeMillis();
        
        for (Entry<Integer, Map<Integer, EpsVO>> entryProducer: producerEpsCache.entrySet()){
            for (Entry<Integer, EpsVO> entryEps: entryProducer.getValue().entrySet()){
                long diff = time - entryEps.getValue().getLastCheckTime();
                entryEps.getValue().setEps(Math.ceil(entryEps.getValue().getCnt() / Math.floor(diff / 1000.d)));
                entryEps.getValue().setDeletedEps(Math.ceil(entryEps.getValue().getDeleted() / Math.floor(diff / 1000.d)));
                entryEps.getValue().setCnt(0);
                entryEps.getValue().setDeleted(0);
                entryEps.getValue().setLastCheckTime(time);
            }
        }
    }
    
    public void newQueue(int port, int queueSize) {
        if (queueSize == 0) {
            queueSize = DEFAULT_QUEUE_MAX;
        }
        this.queue.put(port, new LinkedBlockingQueue<>(queueSize));
    }
    
    public void removeProducerEps(int loggerId) {
        Set<Integer> set = new TreeSet<>(producerEpsCache.keySet());
        for (int port: set) {
            if (this.producerEpsCache.get(port).containsKey(loggerId)) {
                this.producerEpsCache.get(port).remove(loggerId);
            }
        }
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
        for (Entry<Integer, LinkedBlockingQueue<Map<String, Object>>> entry: queue.entrySet()) {
            int port = entry.getKey();
            if (!producerEpsCache.containsKey(port)) {
                producerEpsCache.put(port, new ConcurrentHashMap<>());
            }
            
            if (!producerEpsCache.get(port).containsKey(loggerId)) {
                EpsVO epsVO = new EpsVO();
                epsVO.setName(loggerMgr.getLogger(loggerId).getName());
                epsVO.setLastCheckTime(System.currentTimeMillis());
                producerEpsCache.get(port).put(loggerId, epsVO);
            }
            
            if (entry.getValue().remainingCapacity() == 0) {
                entry.getValue().poll();
                producerEpsCache.get(port).get(loggerId).addDeleted();
            }
            entry.getValue().offer(vo);
            producerEpsCache.get(port).get(loggerId).addCnt();
            
        }
    }

    public List<Map<String, Object>> poll(int port, int maxBuffer) {
        long time = System.currentTimeMillis();
        
        if (!consumerEpsCache.containsKey(port)) {
            EpsVO epsVO = new EpsVO();
            epsVO.setLastCheckTime(time);
            consumerEpsCache.put(port, epsVO);
        }
        
        List<Map<String, Object>> list = new ArrayList<>();
//        while(!queue.get(port).isEmpty() && list.size() < maxBuffer) {
//            list.add(queue.get(port).poll());
//            consumerEpsCache.get(port).addCnt();
//        }
        int cnt = queue.get(port).drainTo(list, maxBuffer);
        consumerEpsCache.get(port).addCnt(cnt);
        
        long diff = time - consumerEpsCache.get(port).getLastCheckTime();
        if (diff > 5 * 1000) {
            consumerEpsCache.get(port).setEps(
                    consumerEpsCache.get(port).getCnt() / (diff / 1000.d));
            consumerEpsCache.get(port).setCnt(0);
            consumerEpsCache.get(port).setLastCheckTime (time);
        }
        return list;
    }
}
