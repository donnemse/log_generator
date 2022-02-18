package com.igloosec.generator.service.socket;

import java.util.Map;
import java.util.Queue;

import com.igloosec.generator.engine.LogVO;

public class SocketServer implements ISocketServer {
    
    private Thread t;
    private SocketServerInstance ssi;
    private int port;
    private Queue<Map<String, Object>> queue;
    
    public SocketServer(int port, Queue<Map<String, Object>> queue) {
        this.port = port;
        this.queue = queue;
    }
    
    @Override
    public void startServer() {
        this.ssi = new SocketServerInstance(this.port, this.queue);
        this.t = new Thread(() -> ssi.start());
        this.t.start();
        
    }

    @Override
    public boolean stopServer() {
        t.interrupt();
        return this.ssi.stop();
    }

    @Override
    public boolean isActive() {
        return this.ssi.isActive();
    }
    

}
