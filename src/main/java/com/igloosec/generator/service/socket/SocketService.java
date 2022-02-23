package com.igloosec.generator.service.socket;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.igloosec.generator.queue.LogQueueService;
import com.igloosec.generator.restful.model.SingleObjectResponse;

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
    
    public SingleObjectResponse open(SocketInfoVO vo) {
        
        if (cache.containsKey(vo.getPort())) {
            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Already opened " + vo.getPort() + " port.");
        } else {
            vo.setServer(new SocketServer(vo.getPort(), queueService));
            vo.getServer().startServer();
            queueService.getQueue(vo.getPort(), vo.getMaxQueueSize());
            this.cache.put(vo.getPort(), vo);
            return new SingleObjectResponse(HttpStatus.OK.value(), "OK");
        }
    }

    public SingleObjectResponse close(int port) {
        if (cache.containsKey(port)) {
            cache.get(port).getServer().stopServer();
            cache.remove(port);
            return new SingleObjectResponse(HttpStatus.OK.value(), "OK");
        }
        return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Could not close " + port + " port.");
    }

    public Collection<SocketInfoVO> list() {
        for (Entry<Integer, Queue<Map<String, Object>>> entry: queueService.getQueue().entrySet()) {
            if(this.cache.containsKey(entry.getKey())) {
                int size = entry.getValue().size();
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

    public SocketInfoVO get(int port) {
        SocketInfoVO vo = this.cache.get(port);
        int size = queueService.getQueue(port, 10000).size();
        vo.setCurrentQueueSize(size);
        vo.setMaxQueueSize(
                size +
                ((LinkedBlockingQueue<Map<String, Object>>) queueService.getQueue(port, 10000)).remainingCapacity());
        vo.setProducerEps(queueService.getProducerEpsCache().get(port));
        vo.setConsumerEps(queueService.getConsumerEpsCache().get(port));
        return vo;
    }

}
