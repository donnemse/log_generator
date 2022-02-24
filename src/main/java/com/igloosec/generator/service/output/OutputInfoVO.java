package com.igloosec.generator.service.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private transient ISocketServer server;
    private long startedTime;
    private long runningTime;
    private String type;
    
    public OutputInfoVO() {
        this.startedTime = System.currentTimeMillis();
        this.openedIp = "System";
        this.type = "TCP";
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
