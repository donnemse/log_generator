package com.yuganji.generator.model;

import com.yuganji.generator.exception.OutputHandleException;
import com.yuganji.generator.output.sparrow.ISocketServer;
import com.yuganji.generator.output.sparrow.TCPSocketServer;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SparrowOutput extends AbstractOutputHandler {
    private int port;
    private transient ISocketServer server;
    
    public SparrowOutput(int port) {
        this.port = port;
    }
    
    @Override
    public boolean startOutput() throws OutputHandleException {
        this.server = new TCPSocketServer(this.port);
        return this.server.startServer();
        
    }
    @Override
    public boolean stopOutput() throws OutputHandleException {
        return this.server.stopServer();
    }
    @Override
    public boolean isRunning() throws OutputHandleException {
        return this.isRunning();
    }
    
    public boolean closeClient(String clientId) {
        return this.server.stopClient(clientId);
    }
    
}
