package com.igloosec.generator.service.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class SocketInfoVO {
    private int port;
    private List<String> clients;
    private Map<Integer, EpsVO> producerEps;
    private EpsVO consumerEps;
    private int maxQueueSize;
    private int currentQueueSize;
    private transient ISocketServer server;
    
    public List<String> getClients(){
        if (clients == null) {
            this.clients = new ArrayList<>();
        }
        return this.clients;
    }
}
