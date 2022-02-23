package com.igloosec.generator.service.output;

public interface ISocketServer {
    void startServer();
    boolean stopServer();
    boolean isActive();
}
