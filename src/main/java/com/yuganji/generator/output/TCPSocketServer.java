package com.yuganji.generator.output;

import java.util.Map;

import io.netty.channel.ChannelHandlerContext;

public class TCPSocketServer implements ISocketServer {
    
    private Thread t;
    private TCPSocketServerInstance ssi;
    private int port;
    private OutputService outputService;
    
    public TCPSocketServer(int port, OutputService outputService) {
        this.port = port;
        this.outputService = outputService;
    }
    
    public TCPSocketServer(OutputService outputService) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void startServer() {
        this.ssi = new TCPSocketServerInstance(this.port, this.outputService);
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
