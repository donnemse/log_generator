package com.igloosec.generator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.igloosec.generator.output.ISocketServer;
import com.igloosec.generator.util.NetUtil;

import lombok.Data;

@Data
public class OutputInfoVO {
    private String openedIp;
    private int port;
    private List<String> clients;
    private Map<Integer, EpsVO> producerEps;
    private EpsVO consumerEps;
    private int maxQueueSize;
    private int currentQueueSize;
    private long currentQueueByte;
    private transient ISocketServer server;
    private long startedTime;
    private long runningTime;
    private String type;
    transient private LinkedBlockingQueue<Map<String, Object>> queue;
    
    transient private static final int MAX_QUEUE_SIZE = 10_000;
    
    public OutputInfoVO() {
        this.initailize(MAX_QUEUE_SIZE);
    }
    
    public OutputInfoVO(int port) {
        this.port = port;
        this.initailize(MAX_QUEUE_SIZE);
    }
    
    public OutputInfoVO(int port, int maxQueueSize) {
        this.port = port;
        this.initailize(maxQueueSize);
    }
    
    private void initailize(int maxQueueSize) {
        this.startedTime = System.currentTimeMillis();
        this.openedIp = NetUtil.getLocalHostIp();
        this.maxQueueSize = maxQueueSize;
        this.queue = new LinkedBlockingQueue<>(this.maxQueueSize);
        this.producerEps = new ConcurrentHashMap<>();
    }
    
    public long getRunningTime() {
        this.runningTime = System.currentTimeMillis() - this.startedTime;
        return runningTime;
    }
    
    public List<String> getClients(){
        if (this.getServer() == null) {
            this.clients = new ArrayList<>();
        } else {
            this.clients = new ArrayList<String>(this.getServer().getClients().keySet());
        }
        return this.clients;
    }
}
