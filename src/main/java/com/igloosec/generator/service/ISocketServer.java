package com.igloosec.generator.service;

public interface ISocketServer {
    void startServer();
    boolean stopServer();
    boolean isActive();
}
