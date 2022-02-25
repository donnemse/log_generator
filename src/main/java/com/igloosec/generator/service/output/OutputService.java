package com.igloosec.generator.service.output;

import java.util.Collection;
import java.util.Date;
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

import com.igloosec.generator.mybatis.mapper.HistoryMapper;
import com.igloosec.generator.queue.LogQueueService;
import com.igloosec.generator.restful.model.SingleObjectResponse;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class OutputService {
    private static final String TYPE = "output";
    
    private Map<Integer, OutputInfoVO> cache;
    
    @Autowired
    private LogQueueService queueService;
    
    @Autowired
    private HistoryMapper histMapper;
    
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
    
    public SingleObjectResponse open(OutputInfoVO vo) {
        
        if (cache.containsKey(vo.getPort())) {
            String message = "Already opened " + vo.getPort() + " port.";
            histMapper.insertHistory(vo.getPort(), vo.getOpenedIp(), TYPE, new Date().getTime(), message, null, null);
            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
        } else {
            vo.setServer(new TCPSocketServer(vo.getPort(), queueService));
            vo.getServer().startServer();
            queueService.getQueue(vo.getPort(), vo.getMaxQueueSize());
            this.cache.put(vo.getPort(), vo);
            String message = "Sucessfully opened " + vo.getPort() + " port.";
            histMapper.insertHistory(vo.getPort(), vo.getOpenedIp(), TYPE, new Date().getTime(), message, null, null);
            return new SingleObjectResponse(HttpStatus.OK.value(), message);
        }
    }

    public SingleObjectResponse close(int port, String ip) {
        if (cache.containsKey(port)) {
            cache.get(port).getServer().stopServer();
            cache.remove(port);
            queueService.removeQueue(port);
            String message = "Sucessfully closed " + port + " port.";
            histMapper.insertHistory(port, ip, TYPE, new Date().getTime(), message, null, null);
            return new SingleObjectResponse(HttpStatus.OK.value(), message);
        }
        String message = "Could not close " + port + " port.";
        histMapper.insertHistory(port,ip, TYPE, new Date().getTime(), message, null, null);
        return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }

    public Collection<OutputInfoVO> list() {
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

    public OutputInfoVO get(int port) {
        OutputInfoVO vo = this.cache.get(port);
        int size = queueService.getQueue(port, 10000).size();
        vo.setCurrentQueueSize(size);
        vo.setMaxQueueSize(
                size +
                ((LinkedBlockingQueue<Map<String, Object>>) queueService.getQueue(port, 10000)).remainingCapacity());
        vo.setProducerEps(queueService.getProducerEpsCache().get(port));
        vo.setConsumerEps(queueService.getConsumerEpsCache().get(port));
        return vo;
    }
    
    public SingleObjectResponse stopClient(int port, String id) {
        if (!this.cache.containsKey(port) ||
                this.cache.get(port).getServer() == null) {
            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), port + " is not opened");
        }
        if (!this.cache.get(port).getServer().stopClient(id)) {
            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "server error");
        }
        return new SingleObjectResponse(HttpStatus.OK.value(), "successfully stopped");
    }

}
