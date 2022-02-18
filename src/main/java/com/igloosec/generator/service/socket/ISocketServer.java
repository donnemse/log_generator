package com.igloosec.generator.service.socket;

public interface ISocketServer {
    void startServer();
    boolean stopServer();
    boolean isActive();
}
