package com.igloosec.generator.service.socket;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.igloosec.generator.queue.LogQueueService;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class SocketService {
    
    private Map<Integer, SocketInfoVO> cache;
    
    @Autowired
    private LogQueueService queueService;
    
    @PostConstruct
    public void init() {
        this.cache = new HashMap<>();
        
    }
    
    @Scheduled(fixedDelay = 15 * 1000)
    public void schedule() {
        for (Entry<Integer, Queue<Map<String, Object>>> entry: queueService.getQueue().entrySet()) {
            if(this.cache.containsKey(entry.getKey())) {
                int size = entry.getValue().size();
                this.cache.get(entry.getKey()).setCurrentQueueSize(size);
                this.cache.get(entry.getKey()).setMaxQueueSize(
                        size +
                        ((LinkedBlockingQueue<Map<String, Object>>)entry.getValue()).remainingCapacity());
                this.cache.get(entry.getKey()).setProducerEps(queueService.getProducerEpsCache().get(entry.getKey()));
                this.cache.get(entry.getKey()).setConsumerEps(queueService.getConsumerEpsCache().get(entry.getKey()));
            }
        }
    }
    
    public boolean open(SocketInfoVO vo) {
        if (cache.containsKey(vo.getPort())) {
            return false;
        } else {
            vo.setServer(new SocketServer(vo.getPort(), queueService));
            vo.getServer().startServer();
            queueService.getQueue(vo.getPort(), vo.getMaxQueueSize());
            this.cache.put(vo.getPort(), vo);
            return true;
        }
    }

    public boolean close(int port) {
        if (cache.containsKey(port)) {
            cache.get(port).getServer().stopServer();
            cache.remove(port);
            return true;
        }
        return false;
    }

    public Collection<SocketInfoVO> list() {
        for (Entry<Integer, Queue<Map<String, Object>>> entry: queueService.getQueue().entrySet()) {
            if(this.cache.containsKey(entry.getKey())) {
                int size = entry.getValue().size();
                log.debug("####### scheduled " + size);
                this.cache.get(entry.getKey()).setCurrentQueueSize(size);
                this.cache.get(entry.getKey()).setMaxQueueSize(
                        size +
                        ((LinkedBlockingQueue<Map<String, Object>>) entry.getValue()).remainingCapacity());
                this.cache.get(entry.getKey()).setProducerEps(queueService.getProducerEpsCache().get(entry.getKey()));
                this.cache.get(entry.getKey()).setConsumerEps(queueService.getConsumerEpsCache().get(entry.getKey()));
                
            }
        }
        return this.cache.values();
    }

}
