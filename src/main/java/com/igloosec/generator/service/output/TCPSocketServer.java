package com.igloosec.generator.service.output;

import java.util.Map;

import com.igloosec.generator.queue.LogQueueService;

import io.netty.channel.ChannelHandlerContext;

public class TCPSocketServer implements ISocketServer {
    
    private Thread t;
    private TCPSocketServerInstance ssi;
    private int port;
    private LogQueueService queueService;
    
    public TCPSocketServer(int port, LogQueueService queueService) {
        this.port = port;
        this.queueService = queueService;
    }
    
    @Override
    public void startServer() {
        this.ssi = new TCPSocketServerInstance(this.port, this.queueService);
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

    @Override
    public Map<String, ChannelHandlerContext> getClients() {
        return this.ssi.getClients();
    }

    @Override
    public boolean stopClient(String id) {
        return ssi.stopClient(id);
    }
    

}
