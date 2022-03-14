package com.yuganji.generator.output.model;

import com.yuganji.generator.exception.OutputHandleException;
import com.yuganji.generator.model.AbstractOutputHandler;
import com.yuganji.generator.output.sparrow.ISocketServer;
import com.yuganji.generator.output.sparrow.TCPSocketServer;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class SparrowOutput extends AbstractOutputHandler {
    private transient final String KEY_PORT = "port";
    private int port;
    private transient int id;
    private transient ISocketServer server;
    
    public SparrowOutput(int id, Map<String, Object> conf) {
        this.id = id;
        this.port = (int) conf.get(this.KEY_PORT);
    }

    public SparrowOutput(int port) {
        this.port = port;
    }
    
    @Override
    public boolean startOutput() throws OutputHandleException {
        this.server = new TCPSocketServer(this.id, this.port);
        return this.server.startServer();
    }

    @Override
    public boolean stopOutput() throws OutputHandleException {
        return this.server.stopServer();
    }

    @Override
    public boolean isRunning() throws OutputHandleException {
        if (this.server == null) {
            return false;
        }

        return server.isRunning();
    }
    
    public boolean closeClient(String clientId) {
        return this.server.stopClient(clientId);
    }
    
}
