package com.igloosec.generator.service.socket;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SocketInfoVO {
    private int port;
    private List<String> clients;
    private EpsVO eps;
    private int maxQueueSize;
    private int currentQueueSize;
    private transient ISocketServer server;
    
    public List<String> getClients(){
        if (clients == null) {
            this.clients = new ArrayList<>();
        }
        return this.clients;
    }
    
    public EpsVO getEps(){
        if (eps == null) {
            this.eps = new EpsVO();
        }
        return this.eps;
    }
}
