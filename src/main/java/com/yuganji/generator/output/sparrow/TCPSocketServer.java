package com.yuganji.generator.output.sparrow;

import java.util.Map;

import com.yuganji.generator.exception.OutputHandleException;

import io.netty.channel.ChannelHandlerContext;

public class TCPSocketServer implements ISocketServer {
    
    private Thread t;
    private TCPSocketServerInstance ssi;
    private int port;
    private int id;
    
    public TCPSocketServer(int id, int port) {
        this.id = id;
        this.port = port;
    }

    @Override
    public boolean startServer() throws OutputHandleException {
        
        // TODO check already open port
        this.ssi = new TCPSocketServerInstance(this.id, this.port);
        this.t = new Thread(() -> ssi.start());
        this.t.start();
        return true;
    }

    @Override
    public boolean stopServer() {
        t.interrupt();
        return this.ssi.stop();
    }

    @Override
    public boolean isRunning() {
        return this.ssi.isRunning();
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
