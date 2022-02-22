package com.igloosec.generator.service.socket;

import com.igloosec.generator.queue.LogQueueService;

public class SocketServer implements ISocketServer {
    
    private Thread t;
    private SocketServerInstance ssi;
    private int port;
    private LogQueueService queueService;
    
    public SocketServer(int port, LogQueueService queueService) {
        this.port = port;
        this.queueService = queueService;
    }
    
    @Override
    public void startServer() {
        this.ssi = new SocketServerInstance(this.port, this.queueService);
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
