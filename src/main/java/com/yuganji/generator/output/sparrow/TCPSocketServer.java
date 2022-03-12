package com.yuganji.generator.output.sparrow;

import java.util.Map;

import com.yuganji.generator.exception.OutputHandleException;
import com.yuganji.generator.output.OutputService;

import io.netty.channel.ChannelHandlerContext;

public class TCPSocketServer implements ISocketServer {
    
    private Thread t;
    private TCPSocketServerInstance ssi;
    private int port;
    
    public TCPSocketServer(int port) {
        this.port = port;
    }
    
    public TCPSocketServer(OutputService outputService) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean startServer() throws OutputHandleException {
        
        // TODO check already open port
        this.ssi = new TCPSocketServerInstance(this.port);
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
