package com.igloosec.generator.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloosec.generator.queue.LogQueueService;
import com.igloosec.generator.service.socket.ISocketServer;
import com.igloosec.generator.service.socket.SocketServer;

@Service
public class SocketService {
    
    private Map<Integer, ISocketServer> cache;
    
    @Autowired
    private LogQueueService queueService;
    
    @PostConstruct
    public void init() {
        this.cache = new HashMap<>();
        
    }
    
    public void open(int port) {
        if (cache.containsKey(port)) {
            return;
        } else {
            ISocketServer t = new SocketServer(port, queueService.getQueue(port));
            t.startServer();
            this.cache.put(port, t);
            
        }
    }

    public void close(int port) {
        if (cache.containsKey(port)) {
            cache.get(port).stopServer();
            cache.remove(port);
        }
    }

}
